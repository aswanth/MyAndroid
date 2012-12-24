package com.qburst.docphin.utilities;

import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;

public class DocphinWebClient extends WebViewClient

{
    ProgressBar progressBar;
    WebView web;

    public DocphinWebClient(ProgressBar progressBar)
    {
        super();
        this.progressBar = progressBar;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon)
    {

        super.onPageStarted(view, url, favicon);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {

        view.loadUrl(url);
        return true;

    }

    @Override
    public void onPageFinished(WebView view, String url)
    {
        // TODO Auto-generated method stub
        super.onPageFinished(view, url);

        progressBar.setVisibility(View.GONE);
    }

}
