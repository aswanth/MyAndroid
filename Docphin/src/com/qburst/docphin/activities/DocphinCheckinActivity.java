package com.qburst.docphin.activities;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ProgressBar;

import com.qburst.docphin.R;
import com.qburst.docphin.activities.base.DocphinBaseActivity;
import com.qburst.docphin.utilities.DocphinWebClient;

public class DocphinCheckinActivity extends DocphinBaseActivity
{
	 private View _checkinLayout;
	    private ViewGroup tabBar;
	    private Button paneButton;
	    private WebView checkinWebView;
	    private ProgressBar _webViewprogresBar;

	    @Override
	    public void onCreate(Bundle savedInstanceState)
	    {
	        super.onCreate(savedInstanceState);

	        initViews();
	        initClickListners();
	        setLayout(_checkinLayout, paneButton);
	    }

		private void initViews()
	    {
	    	_checkinLayout = inflater.inflate(R.layout.docphin_checkin, null);

	        tabBar = (ViewGroup) _checkinLayout.findViewById(R.id.paneLayout);
	        paneButton = (Button) tabBar.findViewById(R.id.paneButton);
	        paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
	                _menuLayout));

	        checkinWebView = (WebView) _checkinLayout.findViewById(R.id.checkinWebView);

	        _webViewprogresBar =
	                (ProgressBar) _checkinLayout.findViewById(R.id.progressBar2);

	        checkinWebView.setWebViewClient(new DocphinWebClient(_webViewprogresBar));
	        checkinWebView.getSettings().setJavaScriptEnabled(true);
	        checkinWebView.loadUrl(getResources().getString(R.string.checkin_URL));
	    }

	    private void initClickListners()
	    {
	        paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
	                _menuLayout));
	    }

	    // To handle "Back" key press event for WebView to go back to previous screen.
	    @Override
	    public boolean onKeyDown(int keyCode, KeyEvent event)
	    {
	        if ((keyCode == KeyEvent.KEYCODE_BACK) && checkinWebView.canGoBack()) {
	        	checkinWebView.goBack();
	            return true;
	        }
	        return super.onKeyDown(keyCode, event);
	    }

		@Override
		public void onBackPressed() {
			// TODO Auto-generated method stub
			super.onBackPressed();
			finish();
		}
	    
}
