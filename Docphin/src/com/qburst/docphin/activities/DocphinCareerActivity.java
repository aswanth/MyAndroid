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

public class DocphinCareerActivity extends DocphinBaseActivity
{

    private View _careerLayout;
    private ViewGroup tabBar;
    private Button paneButton;
    private WebView careerWebView;
    private ProgressBar _webViewprogresBar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initViews();
        initClickListners();
        setLayout(_careerLayout, paneButton);
    }

    private void initViews()
    {
        _careerLayout = inflater.inflate(R.layout.docphin_career, null);

        tabBar = (ViewGroup) _careerLayout.findViewById(R.id.paneLayout);
        paneButton = (Button) tabBar.findViewById(R.id.paneButton);
        paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
                _menuLayout));

        careerWebView = (WebView) _careerLayout.findViewById(R.id.careerWebView);

        _webViewprogresBar =
                (ProgressBar) _careerLayout.findViewById(R.id.progressBar1);

        careerWebView.setWebViewClient(new DocphinWebClient(_webViewprogresBar));
        careerWebView.getSettings().setJavaScriptEnabled(true);
        careerWebView.loadUrl(getResources().getString(R.string.career_URL));
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
        if ((keyCode == KeyEvent.KEYCODE_BACK) && careerWebView.canGoBack()) {
            careerWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

}
