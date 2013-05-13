package com.ingogo.android.activities;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import com.ingogo.android.R;
import com.ingogo.android.adapters.IGCancelReasonAdapter;
import com.ingogo.android.app.IGApiConstants;
import com.ingogo.android.app.IGConstants;
import com.ingogo.android.model.IGJob;
import com.ingogo.android.utilities.IGUtility;
import com.ingogo.android.webservices.IGCancelJobApi;
import com.ingogo.android.webservices.IGCancellationReasonApi;
import com.ingogo.android.webservices.beans.response.IGCancelJobResponseBean;
import com.ingogo.android.webservices.interfaces.IGCancelJobListener;
import com.ingogo.android.webservices.interfaces.IGExceptionApiListener;

public class IGCancellationReasonActivity extends IGBaseActivity implements IGExceptionApiListener, IGCancelJobListener{

	private ListView _cancelReasonLV;
	private ProgressDialog _progressDialog;
	public static String _cancellationReasonKey;
	public static Button _continueButton;
	private IGJob _job;
	// To store cancellation reason which got by calling cancellation reason web
	// service.
	private Map<String, String> _reasonHashMap = new HashMap<String, String>();
	private boolean _responsePendingInCancellationReason;

	private boolean isResponsePendingInCancellationReason() {
		return _responsePendingInCancellationReason;
	}

	private void setResponsePendingInCancellationReason(
			boolean responsePendingInCancellationReason) {
		_responsePendingInCancellationReason = responsePendingInCancellationReason;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		setContentView(R.layout.cancellation_reason);
		initView();
		Intent intent = getIntent();
		_job = (IGJob) intent.getSerializableExtra("Job");
		// Invoke cancellation reason web service when
		// IGCancellationReasonActivity created.
		inVokeCancellationReasonApi();
	}

	protected void onResume() {
		super.onResume();
		// When the app enters back ground and a the progress dialogue is shown
		// then a flag is set true in IngogoApp. So if that
		// flag is true then show the progress dialog on onResume.
		if (isResponsePendingInCancellationReason()) {
			setResponsePendingInCancellationReason(false);
			_progressDialog = IGUtility.showProgressDialog(this);
		}
	}

	protected void onPause() {
		if (_progressDialog != null) {

			// When the app enters back ground and a the progress dialogue is
			// shown then a flag is set true in IngogoApp. So if that
			// flag is true then show the progress dialog on onResume.
			if (_progressDialog.isShowing())
				_progressDialog.dismiss();
			_progressDialog = null;

		}
		super.onPause();
	}

	/**
	 * Initialises the view widget.
	 */
	private void initView() {
		_cancelReasonLV = (ListView) findViewById(R.id.cancel_reason_list_view);
		_continueButton = (Button) findViewById(R.id.continue_button);
	}

	/**
	 * Setting up of view widget.
	 */
	private void setUpViews() {
		// Custom adapter class to show the details got from cancellation reason
		// web service.
		IGCancelReasonAdapter reasonAdapter = new IGCancelReasonAdapter(this,
				_reasonHashMap);
		_cancelReasonLV.setAdapter(reasonAdapter);
		_cancelReasonLV.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapterView, View view,
					int pos, long arg3) {
				view.findViewById(R.id.radioButton).setBackgroundResource(R.drawable.radio_sel);
				if(IGCancelReasonAdapter._lastClickedButton != null && IGCancelReasonAdapter._lastClickedButton != view.findViewWithTag(pos)) {
						IGCancelReasonAdapter._lastClickedButton.setBackgroundResource(R.drawable.radio_un);
					}
				_cancellationReasonKey = _reasonHashMap.keySet().toArray()[pos].toString();
				_continueButton.setEnabled(true);
				IGCancelReasonAdapter._lastClickedButton = view.findViewWithTag(pos);
			}
		});
		
	}

	/**
	 * To invoke cancellation reason web service.
	 */
	private void inVokeCancellationReasonApi() {
		if (IGUtility.isNetworkAvailable(this)) {
			Log.i("Cancel reason key", "" + _cancellationReasonKey);
			_progressDialog = IGUtility.showProgressDialog(this);
			IGCancellationReasonApi igCancellationReasonApi = new IGCancellationReasonApi(
					this);
			igCancellationReasonApi.cancellationReason();
		} else {
			// if there is no network in the device,then prompt an alert.
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * Click on return button is got in this method.
	 * 
	 * @param view
	 */
	public void onReturnButtonClick(View view) {
		Log.i("return button tapped", "return button tapped");
		// To finish the activity and returned to job details page.
		finish();
	}

	/**
	 * Click on continue button is got in this method.
	 * 
	 * @param view
	 */
	public void onContinueButtonClick(View view) {

		Log.i("continue button tapped", "continue button tapped");
		// Prompt an alert with two button.
		showCancelJobAlert();

	}

	/**
	 * Override onPrepareOptionsMenu to disable menu button.
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		menu.clear();
		return false;
	}

	@Override
	public void onResponseReceived(Map<String, Object> response, int apiID) {
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInCancellationReason(false);
		// Successful response of cancellation reason web service.
		if (apiID == IGApiConstants.kCancellationReasonWebServiceId) {
			Log.i("CANCELLATION REASON RESPONSE", response.toString());
			processCancellationReasonResponse(response);
			setUpViews();

		}
//		// Successful response of cancel job web service.
//		else if (apiID == IGApiConstants.kCancelJobWebServiceId) {
//			Log.i("CANCEL JOB RESPONSE", response.toString());
//			processCancelJobResponse(response);
//		}
		// Call onResponseReceived method in base activity.
		super.onResponseReceived(response, apiID);
	}

	@Override
	public void onFailedToGetResponse(Map<String, Object> errorResponse,
			int apiID) {

		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInCancellationReason(false);
		// Call onFailedToGetResponse method in base activity.
		super.onFailedToGetResponse(errorResponse, apiID);
	}

	/**
	 * To process the response got by calling cancellation reason web service.
	 * 
	 * @param response
	 */
	private void processCancellationReasonResponse(Map<String, Object> response) {
		if (response != null && response.containsKey(IGConstants.kDataKey)) {
			if ((JSONObject) response.get(IGConstants.kDataKey) != null) {
				JSONObject data = (JSONObject) response
						.get(IGConstants.kDataKey);
				if (data.has(IGConstants.kReasonKey)
						&& !data.isNull(IGConstants.kReasonKey)) {
					try {
						JSONObject reason = (JSONObject) data
								.getJSONObject(IGConstants.kReasonKey);
						Iterator<String> iterator = reason.keys();
						while (iterator.hasNext()) {
							String cancellationKey = iterator.next();
							_reasonHashMap.put(cancellationKey,
									reason.getString(cancellationKey));
						}

					} catch (JSONException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
	}

	/**
	 * To process the response got by calling cancel job web service.
	 * 
	 * @param response
	 */
	private void processCancelJobResponse(IGCancelJobResponseBean response) {
		if (response != null) {
			displayJobCancelledAlert();
		}
	}

	/**
	 * Navigate to job activity and clear any activity in between job activity
	 * and cancellation reason activity.
	 */

	private void goToJobActivity() {
		Intent intent = new Intent(IGCancellationReasonActivity.this,
				IGJobsActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	/**
	 * An alert with two buttons.If click 'YES', invoke cancel job web service.
	 * If click 'NO', cancel the dialog.
	 */
	private void showCancelJobAlert() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(getString(R.string.CancelBookingTitle))
				.setCancelable(false)
				.setPositiveButton("Yes",
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// To invoke cancel job web service.
								cancelJob();
							}
						})
				.setNegativeButton("No", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						finish();
					}
				});

		AlertDialog alert = builder.create();
		alert.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		alert.show();

	}

	/**
	 * To invoke cancel job web service.
	 */
	private void cancelJob() {

		if (IGUtility.isNetworkAvailable(this)) {

			_progressDialog = IGUtility.showProgressDialog(this);
			IGCancelJobApi igCancelJobApi = new IGCancelJobApi(this, this);
			igCancelJobApi.cancelJob(_cancellationReasonKey, _job.getId());
		} else {
			// If there is no network in the device,prompt an alert.
			IGUtility.showDialogOk(this.getText(R.string.network_error_title)
					.toString(), this.getText(R.string.ReachabilityMessage)
					.toString(), this);
		}

	}

	/**
	 * if the job cancelled web service response is success, prompt an alert
	 * with message job cancelled. Navigate to jobs activity,when click 'OK'.
	 */
	private void displayJobCancelledAlert() {
		AlertDialog.Builder alertbox = new AlertDialog.Builder(this);
		alertbox.setMessage(getString(R.string.job_cancelled_alert));
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
				// Set the job status to cancel.
				_job.setStatus(IGConstants.kJobCancelled);
				// Remove the value in jobInProgress to view the notification
				// for driver stale state.
				// Notification for driver stale state is invoked from
				// IGUpdatePositionPollingTask when there
				// is no job in progress.
				IGUtility.removeDefaults(IGConstants.kJobInProgress,
						IGCancellationReasonActivity.this);
				goToJobActivity();
			}
		});
		AlertDialog dialog = alertbox.create();
		dialog.setVolumeControlStream(AudioManager.STREAM_MUSIC);
		dialog.show();
	}

	public void onBackPressed() {
		finish();
	}

	@Override
	public void jobCancelledSuccessfully(IGCancelJobResponseBean response) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInCancellationReason(false);

		// Successful response of cancel job web service.
		Log.i("CANCEL JOB RESPONSE", response.toString());
		processCancelJobResponse(response);
		
	}

	@Override
	public void jobCancellationFailed(String errorMessage) {
		// TODO Auto-generated method stub
		IGUtility.dismissProgressDialog(_progressDialog);
		setResponsePendingInCancellationReason(false);
	}

	@Override
	public void onNetWorkUnavailableResponse(Map<String, Object> errorResponse) {
		
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNetWorkUnavailableResponse(errorResponse);
	}

	@Override
	public void onRequestTimedoutResponse(Map<String, Object> errorResponse) {
		setResponsePendingInCancellationReason(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onRequestTimedoutResponse(errorResponse);
	}

	@Override
	public void onInternalServerErrorResponse(Map<String, Object> errorResponse) {
		setResponsePendingInCancellationReason(false);
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onInternalServerErrorResponse(errorResponse);

	}

	@Override
	public void onNullResponseRecieved() {
		IGUtility.dismissProgressDialog(_progressDialog);
		super.onNullResponseRecieved();

	}
}