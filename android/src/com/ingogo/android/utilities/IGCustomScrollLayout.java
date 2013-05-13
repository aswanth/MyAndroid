package com.ingogo.android.utilities;

import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ScrollView;

public class IGCustomScrollLayout extends ScrollView {

	public IGCustomScrollLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public interface Listener {
		public void onSoftKeyboardShown(boolean isShowing);
	}

	private Listener listener;

	public void setListener(Listener listener) {
		this.listener = listener;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int height = MeasureSpec.getSize(heightMeasureSpec);
		Activity activity = (Activity) getContext();
		Rect rect = new Rect();
		activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
		int statusBarHeight = rect.top;
		int screenHeight = activity.getWindowManager().getDefaultDisplay()
				.getHeight();
		int diff = (screenHeight - statusBarHeight) - height;
		Log.i("height", "" + height);
		Log.i("statusBarHeight", "" + statusBarHeight);
		Log.i("screenHeight", "" + screenHeight);
		if (listener != null) {
			listener.onSoftKeyboardShown(diff > 128); // assume all soft
														// keyboards are at
														// least 128 pixels high
		}
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);

	}

}
