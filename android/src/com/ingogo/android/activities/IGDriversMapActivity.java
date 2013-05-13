package com.ingogo.android.activities;

import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;
import com.ingogo.android.R;
import com.ingogo.android.activities.payments.IGPaymentsSwipeActivity;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.app.IngogoApp;
import com.ingogo.android.app.IngogoApp.jobStatusEnum;
import com.ingogo.android.map.IGCustomMapOverlay;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.poll.IGChatService;
import com.ingogo.android.poll.IGDriverMapPollingTask;
import com.ingogo.android.poll.IGUpdatePositionPollingTask;
import com.ingogo.android.utilities.IGLocationListener;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCollectedJobApi;
import com.ingogo.android.webservices.IGCompletedJobApi;
import com.ingogo.android.webservices.IGCreditDetailsApi;
import com.ingogo.android.webservices.IGResponseListener;
import com.ingogo.android.webservices.beans.response.IGCollectJobResponseBean;
import com.ingogo.android.webservices.beans.response.IGMapInfoResponseBean;
import com.ingogo.android.webservices.interfaces.IGCollectJobListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;
import com.ingogo.android.webservices.interfaces.IGMapInfoApiListener;

public class IGDriversMapActivity extends IGBaseMapActivity implements
		OnTouchListener, IGResponseListener, IGMapInfoApiListener,
		IGCollectJobListener, IGExceptionApiListener {

	private MapView _mapView;
	LocationManager _locationManager = null;
	Location _location = null;
	double _latitude, _longitude, _passengerLatitude, _passengerLongitude;
	private MapController _mc;
	private Drawable _passengerLocDrawable = null;
	private Drawable _driverCabLocDrawable = null;
	private static int _mapZoom = 18;
	List<Overlay> mapOverlays = null;
	long lastTouchTime;
	private boolean _isTouchIsOnMap;
	private IGCustomMapOverlay _itemizedOverlay = null;
	IGMapInfoResponseBean _mapInfo;
	private String _passengerAddress, _passengerNumber;
	private IGJob _job;
	private ProgressDialog _progressDialog;
	private ImageButton _collectedButton;
	public static boolean _collectedInTime;
	private Date date = null;
	private IGDriverMapPollingTask _pollingTask;
	private TextView _nameTv, _addressTv;
	private int CHAT_ACTIVITY_ID = 69;
	private boolean _callOnApproach = false;
	double _prevLat = 0.0;
	double _prevLong = 0.0;
	boolean _isZoomed = false;

	private boolean _hasCreditCards;
	boolean _navigateToCalculator = false;
	JSONObject _bookingDetails;

	@Override
	protected void onCreate(Bundle icicle) {
		// TODO Auto-generated method stub
		super.onCreate(icicle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.driver_map);
		getBundleExtras();

		refreshLocationValues();

		initViews();
		_mapView.setOnTouchListener(this);

		_mapView.removeAllViews();
		mapOverlays.removeAll(mapOverlays);
		_mapView.getOverlays().clear();
		_mapView.invalidate();
		_mapView.getOverlays().add(new TouchOverlay());

		setUpViews();
	}

	private void getBundleExtras() {
		_mapInfo = (IGMapInfoResponseBean) getIntent().getSerializableExtra(
				IGConstants.kMapInfo);
		_passengerLatitude = _mapInfo.getPickupLatitude().doubleValue();
		_passengerLongitude = _mapInfo.getPickupLongitude().doubleValue();

		_callOnApproach = _mapInfo.getCallOnApproach();

		if (_mapInfo.getPassengerMobileNo() != null)
			_passengerNumber = ", " + _mapInfo.getPassengerMobileNo();
		else
			_passengerNumber = "";

		_job = (IGJob) getIntent().getSerializableExtra(IGConstants.kJob);
		_passengerAddress = _job.getPickupFrom();

		if (_mapInfo.getMessages().size() > 0) {
			Intent intent = new Intent(this, IGChatActivity.class);
			intent.putExtra("Job", _job);
			intent.putExtra("content", _mapInfo.getMessages());
			startActivityForResult(intent, CHAT_ACTIVITY_ID);
		}
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		if (_pollingTask != null) {
			_pollingTask.stopPolling();
			_pollingTask = null;
		}
		IGChatService.getInstance(Integer.parseInt(_job.getId())).stop();
		IGUpdatePositionPollingTask.ignoreStaleState = false;
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		IGUpdatePositionPollingTask.ignoreStaleState = true;
		refreshLocationValues();

		startPollingTask();
	}

	private void startPollingTask() {

		// if (_pollingTask == null) {
		_pollingTask = new IGDriverMapPollingTask(_job.getId(), this);
		// }
		_pollingTask.startPolling(IGConstants.kIncomingMessagePollingInterval);

	}

	/**
	 * Function to initialize views
	 */

	private void initViews() {

		_collectedButton = (ImageButton) findViewById(R.id.collectedBtn);
		_nameTv = (TextView) findViewById(R.id.name_textView);
		_addressTv = (TextView) findViewById(R.id.address_textView);

		long timestamp = _job.getTimeStamp();
		date = new Date(timestamp);

		_latitude = IGLocationListener.getCurrentLatitude();
		_longitude = IGLocationListener.getCurrentLongitude();
		_mapView = (MapView) findViewById(R.id.mapview);
		_mapView.setSatellite(false);
		_locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		_location = new Location(LocationManager.NETWORK_PROVIDER);
		_passengerLocDrawable = getResources().getDrawable(
				R.drawable.nearby_icon);
		_driverCabLocDrawable = getResources().getDrawable(
				R.drawable.available_icon);

		_mc = _mapView.getController();
		mapOverlays = _mapView.getOverlays();
		mapOverlays.clear();
	}

	private void setUpViews() {

		if (_callOnApproach == true)
			_nameTv.setText(_mapInfo.getName() + _passengerNumber);
		else
			_nameTv.setText(_mapInfo.getName());

		_addressTv.setText(_passengerAddress);

		markMyLocOnMap();
		markPassengerOnMap(true);

		_prevLat = _latitude;
		_prevLong = _longitude;
	}

	private void markMyLocOnMap() {
		_itemizedOverlay = new IGCustomMapOverlay(_driverCabLocDrawable, this,
				"driver", _mapView);
		GeoPoint point = new GeoPoint((int) (_latitude * 1000000),
				(int) (_longitude * 1000000));
		OverlayItem overlayItem = new OverlayItem(point, "", "");
		_itemizedOverlay.addOverlay(overlayItem);
		mapOverlays.add(_itemizedOverlay);
	}

	private void markPassengerOnMap(boolean isZoomRequired) {

		try {

			_itemizedOverlay = new IGCustomMapOverlay(_passengerLocDrawable,
					this, "passenger", _mapView);

			GeoPoint point = new GeoPoint((int) (_passengerLatitude * 1000000),
					(int) (_passengerLongitude * 1000000));

			OverlayItem overlayitem = new OverlayItem(point, _passengerAddress,
					_passengerNumber);
			_itemizedOverlay.addOverlay(overlayitem);
			mapOverlays.add(_itemizedOverlay);
			final MapController mc = _mapView.getController();

			int lat = (int) (_latitude * 1000000);
			int lon = (int) (_longitude * 1000000);
			int passengerlat = (int) (_passengerLatitude * 1000000);
			int passengerlong = (int) (_passengerLongitude * 1000000);

			// _mc.setCenter(new GeoPoint(passengerlat, passengerlong));
			int maxLat = Math.max(lat, passengerlat);
			int minLat = Math.min(lat, passengerlat);
			int maxLon = Math.max(lon, passengerlong);
			int minLon = Math.min(lon, passengerlong);

			if (isZoomRequired) {

				double fitFactor = 2;
				_mc.zoomToSpan((int) (Math.abs(maxLat - minLat) * fitFactor),
						(int) (Math.abs(maxLon - minLon) * fitFactor));
				_mc.setCenter(new GeoPoint((maxLat + minLat) / 2,
						(maxLon + minLon) / 2));
				// _mc.zoomToSpan(Math.abs(maxLat - minLat),Math.abs(maxLon -
				// minLon));
				// _mc.setZoom(_mapView.getZoomLevel() - 2);

			} else {

				if (_isZoomed == false) {
					if (_prevLat != _latitude || _prevLong != _longitude) {

						double fitFactor = 1.5;
						_mc.zoomToSpan(
								(int) (Math.abs(maxLat - minLat) * fitFactor),
								(int) (Math.abs(maxLon - minLon) * fitFactor));
						_mc.setCenter(new GeoPoint((maxLat + minLat) / 2,
								(maxLon + minLon) / 2));
						// _mc.zoomToSpan(Math.abs(maxLat -
						// minLat),Math.abs(maxLon - minLon));
						// _mc.setZoom(_mapView.getZoomLevel() - 2);

					}
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}

	}

	private void refreshLocationValues() {
		_latitude = IGLocationListener.getCurrentLatitude();
		_longitude = IGLocationListener.getCurrentLongitude();

	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		if (ev.getAction() == MotionEvent.ACTION_DOWN) {

			long thisTime = System.currentTimeMillis();

			if (thisTime - lastTouchTime < 250) {

				// Double tap
				if (_isTouchIsOnMap) {
					_mc.zoomInFixing((int) ev.getX(), (int) ev.getY());
					lastTouchTime = -1;
				}

			} else {

				// Too slow :)
				lastTouchTime = thisTime;
			}
		} else if (ev.getAction() == MotionEvent.ACTION_UP) {
			_isTouchIsOnMap = true;
		}
		return super.dispatchTouchEvent(ev);
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		_isTouchIsOnMap = true;
		return false;
	}

	public void onJobDetailBtnClick(View view) {
		this.finish();
	}

	public void onCollectedBtnClick(View view) {
		_collectedButton.setEnabled(false);

		if (IGUtility.isNetworkAvailable(this)) {
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCollectedJobApi igCollectedJobApi = new IGCollectedJobApi(this,
					this);
			igCollectedJobApi.collectedBooking(_job.getId());
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	public void refocusButtonClick(View view) {
		_isZoomed = false;

		refreshLocationValues();
		_mapView.removeAllViews();
		mapOverlays.removeAll(mapOverlays);
		_mapView.getOverlays().clear();
		_mapView.invalidate();
		_mapView.getOverlays().add(new TouchOverlay());
		markMyLocOnMap();
		markPassengerOnMap(true);

		_prevLat = _latitude;
		_prevLong = _longitude;
	}

	private void collectedInTime() {
		Calendar calendar = Calendar.getInstance();
		Date dateCompleted = new Date(calendar.getTimeInMillis());
		if (date == null)
			return;
		if (!dateCompleted.after(date)) {
			if (dateCompleted.getTime() < date.getTime()
					|| dateCompleted.getTime() == date.getTime()) {
				_collectedInTime = true;
				Log.d("collectedInTime", "collected");
			} else {
				_collectedInTime = false;

			}
		}
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		// TODO Auto-generated method stub
		_collectedButton.setEnabled(true);
		super.onResponseReceived(response, apiID);

		if (response == null) {
			IGUtility.dismissProgressDialog(_progressDialog);
			return;
		}

		JSONObject responseObject = (JSONObject) response
				.get(IGConstants.kDataKey);
		if (apiID == IGApiConstants.kCreditDetailsWebServiceId) {
			IGUtility.dismissProgressDialog(_progressDialog);
			_bookingDetails = responseObject;
			processBookingDetailsResponse(responseObject);
			if (_navigateToCalculator) {
				goToCalculator();
				return;
			}
			setUpViews();

		} else if (apiID == IGApiConstants.kCompletedJobWebServiceId) {
			IngogoApp.setJobStatus(jobStatusEnum.COMPLETED);
			_navigateToCalculator = true;
			callCreditDetailsApi();
		}
	}

	private void gotoPayment() {
		if (IngogoApp.getJobStatus() == jobStatusEnum.COMPLETED) {
			_navigateToCalculator = true;
			callCreditDetailsApi();
			return;
		}

		if (IGUtility.isNetworkAvailable(this)) {
			if (_progressDialog != null && !_progressDialog.isShowing()) {
				_progressDialog = IGUtility.showProgressDialog(this);
			}
			IGCompletedJobApi igCompletedJobApi = new IGCompletedJobApi(this,
					Integer.parseInt(_job.getId()));
			igCompletedJobApi.completed();
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}
	}

	/**
	 * To call credit details web service.
	 */
	private void callCreditDetailsApi() {

		if (IGUtility.isNetworkAvailable(this)) {
			if (_progressDialog != null && !_progressDialog.isShowing()) {
				_progressDialog = IGUtility.showProgressDialog(this);
			}
			IGCreditDetailsApi creditApi = new IGCreditDetailsApi(this,
					Integer.parseInt(_job.getId()));
			creditApi.getCreditDetails();
		} else {
			IGUtility.dismissProgressDialog(_progressDialog);
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	private void goToCalculator() {

		IngogoApp.getSharedApplication().removeStoredMeterFare();
		Intent intent;

		// If the btn tapped is ingogo payment then navigate to the
		// calculator screen otherwise to the offline calculator screen.

		if (_hasCreditCards) {
			intent = new Intent(IGDriversMapActivity.this,
					IGPaymentActivity.class);
		} else {
			intent = new Intent(IGDriversMapActivity.this,
					IGPaymentsSwipeActivity.class);
		}

		intent.putExtra(IGConstants.kJobId, _job.getId());

		if (_bookingDetails != null) {

			HashMap<String, Object> jobDetails = new HashMap<String, Object>();
			jobDetails.put(IGConstants.kDetails, _bookingDetails.toString());

			intent.putExtra(IGConstants.kJobDetails, jobDetails);
		}
		startActivity(intent);
		finish();
	}

	private void processBookingDetailsResponse(JSONObject responseObject) {

		// Check whether the passenger has atleast one card
		// registered with ingogo.
		if (responseObject.has(IGApiConstants.kCardDetails)) {
			try {
				if (responseObject.getJSONArray(IGApiConstants.kCardDetails)
						.length() > 0) {
					_hasCreditCards = true;
				} else {
					_hasCreditCards = false;
				}
			} catch (JSONException e) {
				_hasCreditCards = false;
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {
		// TODO Auto-generated method stub
		_collectedButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onFailedToGetResponse(errorResponse, apiID);
	}

	@Override
	public void mapInfoCompleted(IGMapInfoResponseBean mapInfo) {

		if (mapInfo.getBookingStatus() != null
				&& mapInfo.getBookingStatus().equalsIgnoreCase(
						IGApiConstants.kPassengerCancelledJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_cancelled_alert));
		} else if (mapInfo.getBookingStatus() != null
				&& mapInfo.getBookingStatus().equalsIgnoreCase(
						IGApiConstants.kPassengerNotConfirmedJob)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_notconfirmed_alert));
		} else if (mapInfo.getBookingStatus() != null
				&& mapInfo.getBookingStatus().equalsIgnoreCase(
						IGApiConstants.kPassengerDispatched)) {
			IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
			this.processPassengerCancelledAlert(getString(R.string.passenger_dispatched_alert));
		} else {

			if (mapInfo.getMessages().size() > 0) {
				Intent intent = new Intent(this, IGChatActivity.class);
				intent.putExtra("Job", _job);
				intent.putExtra("content", mapInfo.getMessages());
				startActivityForResult(intent, CHAT_ACTIVITY_ID);
				return;
			}
			refreshLocationValues();
			_mapView.removeAllViews();
			mapOverlays.removeAll(mapOverlays);
			_mapView.getOverlays().clear();
			_mapView.invalidate();
			_mapView.getOverlays().add(new TouchOverlay());
			markMyLocOnMap();
			markPassengerOnMap(false);

			_prevLat = _latitude;
			_prevLong = _longitude;
		}
	}

	/**
	 * While the user was in the chat activity and the passenger cancelled the
	 * job.
	 */

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CHAT_ACTIVITY_ID && resultCode == RESULT_OK
				&& data != null) {
			if (data.getExtras() == null)
				return;
			String bookingStatus = data.getExtras().getString(
					IGApiConstants.kBookingStatusKey);
			if (bookingStatus == null)
				return;
			if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerCancelledJob)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);

				this.processPassengerCancelledAlert(getString(R.string.passenger_cancelled_alert));
			} else if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerNotConfirmedJob)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
				this.processPassengerCancelledAlert(getString(R.string.passenger_notconfirmed_alert));
			} else if (bookingStatus
					.equalsIgnoreCase(IGApiConstants.kPassengerDispatched)) {
				IGUtility.removeDefaults(IGConstants.kJobInProgress, this);
				this.processPassengerCancelledAlert(getString(R.string.passenger_dispatched_alert));
			}
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void mapInfoFailed(String errorMessage) {
		// TODO Auto-generated method stub

	}

	private void processPassengerCancelledAlert(String message) {

		IGChatService.getInstance(
				Integer.parseInt(IGDriversMapActivity.this._job.getId()))
				.stop();

		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(message);
		alertbox.setCancelable(false);
		alertbox.setOnKeyListener(new DialogInterface.OnKeyListener() {
			// To disable the search button.
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH
						&& event.getRepeatCount() == 0) {
					return true;
				}
				return false;
			}

		});
		alertbox.setNeutralButton("OK", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				_job.setStatus(IGConstants.kJobCancelled); // Set status to
															// cancelled.
				goToJobsActivity();
			}
		});
		AlertDialog dialog = alertbox.create();
		dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dialog.show();
	}

	/*
	 * private class TouchOverlay extends com.google.android.maps.Overlay { int
	 * lastZoomLevel = -1;
	 * 
	 * @Override public boolean onTouchEvent(MotionEvent event, MapView mapview)
	 * { if (event.getAction() == 1) { if (lastZoomLevel == -1) lastZoomLevel =
	 * mapview.getZoomLevel();
	 * 
	 * if (mapview.getZoomLevel() != lastZoomLevel) {
	 * onZoom(mapview.getZoomLevel()); lastZoomLevel = mapview.getZoomLevel(); }
	 * } return false; } }
	 */
	public void onZoom(int level) {
		_isZoomed = true;
	}

	class TouchOverlay extends com.google.android.maps.Overlay {
		private boolean isPinch = false;
		int lastZoomLevel = -1;

		@Override
		public boolean onTap(GeoPoint p, MapView map) {
			if (isPinch) {
				onZoom(1);
				return false;
			} else {
				return false; // Null GeoPoint
			}
		}

		@Override
		public boolean onTouchEvent(MotionEvent e, MapView mapView) {
			int fingers = e.getPointerCount();
			if (e.getAction() == MotionEvent.ACTION_DOWN) {
				isPinch = false; // Touch DOWN, don't know if it's a pinch yet
			}
			if (e.getAction() == MotionEvent.ACTION_MOVE && fingers == 2) {
				isPinch = true; // Two fingers, def a pinch
			}

			if (e.getAction() == 1) {
				if (lastZoomLevel == -1)
					lastZoomLevel = mapView.getZoomLevel();

				if (mapView.getZoomLevel() != lastZoomLevel) {
					onZoom(mapView.getZoomLevel());
					lastZoomLevel = mapView.getZoomLevel();
				}
			}
			return super.onTouchEvent(e, mapView);
		}

	}

	@Override
	public void collectedJobSuccessfully(IGCollectJobResponseBean response) {
		// Dialog not dismissed since next api being called.
		collectedInTime();
		setResult(IGConstants.kJobCollected, getIntent());
		_job.setStatus(IGConstants.kJobCollected);
		IngogoApp.setJobStatus(jobStatusEnum.COLLECTED);
		gotoPayment();

	}

	@Override
	public void collectJobRequestFailed(String errorMessage) {
		// TODO Auto-generated method stub
		_collectedButton.setEnabled(true);
		IGUtility.dismissProgressDialog(_progressDialog);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}

}
