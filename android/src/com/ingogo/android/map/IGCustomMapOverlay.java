package com.ingogo.android.map;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.MapView.LayoutParams;
import com.google.android.maps.OverlayItem;
import com.ingogo.android.R;

/**
 * Class Name: IGCollectionInProgressCustomMapOverlay
 * 
 * Class Desc: The custom overlay class for implementing the map overlay related
 * functionality
 * 
 * @author: QBurst
 * 
 */

public class IGCustomMapOverlay extends ItemizedOverlay<OverlayItem> {

	private ArrayList<OverlayItem> _mapOverlays = new ArrayList<OverlayItem>();
	private Context _context;
	private String _status;
	private MapView _mapView;

	private Drawable _marker;
	PopupWindow popUp = null;

	public IGCustomMapOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
	}

	public IGCustomMapOverlay(Drawable defaultMarker, Context context,
			String status, MapView mapview) {
		this(defaultMarker);
		this._context = context;
		this._status = status;
		this._mapView = mapview;
		this._marker = defaultMarker;
	}

	@Override
	protected OverlayItem createItem(int i) {
		return _mapOverlays.get(i);
	}

	@Override
	public int size() {
		return _mapOverlays.size();
	}

	@Override
	public boolean onTap(GeoPoint p, MapView mapView) {

		mapView.removeView(mapView.getChildAt(0));

		if (popUp != null) {
			popUp.dismiss();
			_mapView.removeView(popUp.getContentView());
			popUp = null;
		}
		return super.onTap(p, mapView);

	}

	@Override
	protected boolean onTap(int index) {

		if (popUp != null) {
			_mapView.removeView(popUp.getContentView());
			popUp = null;
		}
		OverlayItem item = _mapOverlays.get(index);

		Point out = null;
		out = _mapView.getProjection().toPixels(item.getPoint(), out);
		LayoutInflater inflater = (LayoutInflater) _context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View contentView = inflater.inflate(
				R.layout.map_custom_popup, null, false);
		TextView addressText = (TextView) contentView
				.findViewById(R.id.addressTextView);
		addressText.setText(item.getTitle());
		
		ImageButton callDriverButton = (ImageButton)contentView
				.findViewById(R.id.callButton);
		
		int poupheight = 0;
		int popupwidth = 0;

		if (_status.equalsIgnoreCase("passenger")) {
			//do nothing

		} else if (_status.equalsIgnoreCase("driver")) {

			callDriverButton.setVisibility(View.GONE);
			contentView.setBackgroundResource(R.drawable.current_pop);
			popupwidth = _context.getResources()
					.getDrawable(R.drawable.current_pop).getMinimumWidth();
			poupheight = _context.getResources()
					.getDrawable(R.drawable.current_pop).getMinimumHeight();
			
			MapView.LayoutParams mapviewparams = new LayoutParams(popupwidth,
					poupheight, item.getPoint(), - (int)(popupwidth * 0.18), -poupheight
							- (int)(poupheight * 0.85), LayoutParams.MODE_VIEW);
			contentView.setLayoutParams(mapviewparams);
			
			popUp = new PopupWindow(contentView, popupwidth, poupheight);
			popUp.setOutsideTouchable(true);
			popUp.setBackgroundDrawable(new BitmapDrawable());
			popUp.setTouchInterceptor(new OnTouchListener() {
				@Override
				public boolean onTouch(View v, MotionEvent event) {
					if (event.getAction() == MotionEvent.ACTION_OUTSIDE
							|| event.getAction() == MotionEvent.ACTION_DOWN) {
						Log.e("outside", "here");
						popUp.dismiss();
						_mapView.removeView(popUp.getContentView());
						return true;
					}
					return true;

				}

			});
			contentView.setVisibility(View.VISIBLE);
			_mapView.addView(popUp.getContentView());
			
		}
		return true;
	}

	/*
	 * add the overlay to the mapoverlays.
	 */

	public void addOverlay(OverlayItem overlay) {
		_mapOverlays.add(overlay);
		this.populate();
	}

	@Override
	public boolean onTouchEvent(MotionEvent event, MapView mapView) {
		return super.onTouchEvent(event, mapView);
	}

	@Override
	public void draw(Canvas canvas, MapView mapView, boolean shadow) {
		super.draw(canvas, mapView, false);
		if (mapView.getZoomLevel() < 2)
			mapView.getController().setZoom(2);
	}

	@Override
	public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
			long when) {
		if (mapView.getZoomLevel() < 2)
			mapView.getController().setZoom(2);
		return super.draw(canvas, mapView, false, when);
	}

}
