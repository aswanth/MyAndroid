package com.ingogo.android.utilities;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;

import com.ingogo.android.R;

public class IGPayOfflineOverlay extends Dialog{

	public IGPayOfflineOverlay(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	/**
	 * Helper class for creating a custom dialog
	 */
	public static class Builder {

		private Context context;
		private String title;
		private View contentView;

		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * Set the Dialog title from String
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTitle(String title) {
			this.title = title;
			return this;
		}

		/**
		 * Set a custom content view for the Dialog. If a message is set, the
		 * contentView is not added to the Dialog...
		 * 
		 * @param v
		 * @return
		 */
		public Builder setContentView(View v) {
			this.contentView = v;
			return this;
		}

		/**
		 * Create the custom dialog
		 */
		public IGPayOfflineOverlay create() {
			LayoutInflater inflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			final IGPayOfflineOverlay dialog = new IGPayOfflineOverlay(context);
			
			View layout = inflater.inflate(R.layout.payoffline_alert, null);

			dialog.addContentView(layout, new LayoutParams(
					LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
			dialog.getWindow().setAttributes(lp);
	
			dialog.setTitle(title);
			dialog.setContentView(layout);
			return dialog;
		}

	}
}
