package com.cocos.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.chrishui.snackbar.ToastKt;
import com.yourong.game.jydhm.R;

public class WebViewActivity extends Activity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_webview_activity);

        String url = getIntent().getStringExtra("url");
        if (TextUtils.isEmpty(url)) {
            runOnUiThread(() -> {
                ToastKt.make(this, "参数错误", ToastKt.LENGTH_SHORT).show();
                finish();
            });
            return;
        }

        String title = getIntent().getStringExtra("title");
        if (!TextUtils.isEmpty(title)) {
            TextView titleView = findViewById(R.id.title);
            titleView.setText(title);
        }

        WebView webView = findViewById(R.id.webView);
        setSettings(webView);
        webView.loadUrl(url);

        ImageView backView = findViewById(R.id.back);
        backView.setOnClickListener(view -> finish());
    }

    @SuppressLint({"SetJavaScriptEnabled", "JavascriptInterface"})
    private void setSettings(WebView webView) {
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                return super.shouldOverrideUrlLoading(view, request);
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onCreateWindow(WebView view, boolean isDialog, boolean isUserGesture, Message message) {
                WebView newWebView = new WebView(view.getContext());
                newWebView.setWebViewClient(new WebViewClient() {
                    @Override
                    public boolean shouldOverrideUrlLoading(WebView view, String url) {
                        // 新窗口处理：
                        // 1、本页面内加载
                        // webView.loadUrl(url);
                        // 2、新页面内加载
                        Intent intent = new Intent(WebViewActivity.this, WebViewActivity.class);
                        intent.putExtra("url", url);
                        startActivity(intent);
                        return true;
                    }
                });
                WebView.WebViewTransport transport = (WebView.WebViewTransport) message.obj;
                transport.setWebView(newWebView);
                message.sendToTarget();
                return true;
            }
        });
        if (WebViewFeature.isFeatureSupported(WebViewFeature.START_SAFE_BROWSING)) {
            WebViewCompat.startSafeBrowsing(webView.getContext(), null);
        }

        WebSettings settings = webView.getSettings();
        settings.setUserAgentString(Build.MODEL + "/CocosGame " + Build.VERSION.RELEASE + "/" + settings.getUserAgentString());

        int uiMode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
        boolean night = uiMode == Configuration.UI_MODE_NIGHT_YES;
        if (night) {
            if (WebViewFeature.isFeatureSupported(WebViewFeature.ALGORITHMIC_DARKENING))
                WebSettingsCompat.setAlgorithmicDarkeningAllowed(settings, true);
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK_STRATEGY))
                WebSettingsCompat.setForceDarkStrategy(settings, WebSettingsCompat.DARK_STRATEGY_PREFER_WEB_THEME_OVER_USER_AGENT_DARKENING);
            if (WebViewFeature.isFeatureSupported(WebViewFeature.FORCE_DARK))
                WebSettingsCompat.setForceDark(settings, WebSettingsCompat.FORCE_DARK_ON);
        }

        // 支持 JavaScript 交互
        settings.setJavaScriptEnabled(true);
        // 设置允许JS弹窗
        settings.setJavaScriptCanOpenWindowsAutomatically(true);

        settings.setBuiltInZoomControls(true);
        settings.setDisplayZoomControls(false); // 不支持放大缩小
        settings.setDatabaseEnabled(true);
        settings.setSaveFormData(true); // 设置 webview 保存表单数据
        // noinspection deprecation
        settings.setSavePassword(true); // 设置 webview 保存密码
        settings.setLoadsImagesAutomatically(true); // 支持自动加载图片
        settings.setDomStorageEnabled(true);
        settings.setGeolocationEnabled(true);
        settings.setLoadWithOverviewMode(true);
        settings.setUseWideViewPort(true); // 设置 webview 推荐使用的窗口，使html界面自适应屏幕
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true); // 设置可以访问文件
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN); // 不支持放大缩小
        settings.setSupportZoom(true);
        settings.setSupportMultipleWindows(true);

        String dir = getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        // 允许Https + Http混用  5.0
        settings.setMixedContentMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(this, "android");
    }
}
