package com.py.producthuntreader.model;

import android.annotation.TargetApi;
import android.net.Uri;
import android.os.Build;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

/**
 * Created by Puzino Yury on 09.03.2017.
 */

public class CustomWebClient extends WebViewClient {

    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        final Uri uri = Uri.parse(url);
        return handleUri(uri);
    }

    @TargetApi(Build.VERSION_CODES.N)
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
        final Uri uri = request.getUrl();
        return handleUri(uri);
    }

    /** Returning false means that you are going to load this url in the webView itself.
     * @param uri - useless
     * @return false - want to view webpages only in webview, no redirections to browser
     * */
    private boolean handleUri(final Uri uri) {
        return false;
    }
}
