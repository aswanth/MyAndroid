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

public class DocphinQuizActivity extends DocphinBaseActivity
{

    private View _quizLayout;
    private ViewGroup tabBar;
    private Button paneButton;
    private WebView quizWebView;
    private ProgressBar _webViewprogresBar;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initViews();
        initClickListners();
        setLayout(_quizLayout, paneButton);
    }

    private void initViews()
    {
        _quizLayout = inflater.inflate(R.layout.docphin_quiz, null);

        tabBar = (ViewGroup) _quizLayout.findViewById(R.id.paneLayout);
        paneButton = (Button) tabBar.findViewById(R.id.paneButton);
        paneButton.setOnClickListener(new ClickListenerForScrolling(mainLayout,
                _menuLayout));

        quizWebView = (WebView) _quizLayout.findViewById(R.id.quizWebView);

        _webViewprogresBar =
                (ProgressBar) _quizLayout.findViewById(R.id.progressBar1);

        quizWebView.setWebViewClient(new DocphinWebClient(_webViewprogresBar));
        quizWebView.getSettings().setJavaScriptEnabled(true);
        quizWebView.loadUrl(getResources().getString(R.string.quiz_URL));
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
        if ((keyCode == KeyEvent.KEYCODE_BACK) && quizWebView.canGoBack()) {
            quizWebView.goBack();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
