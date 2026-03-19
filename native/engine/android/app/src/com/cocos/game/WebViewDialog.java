package com.cocos.game;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import androidx.webkit.WebSettingsCompat;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;

import com.yourong.game.jydhm.R;

public class WebViewDialog extends Dialog {
    private final Context context;

    private final String url;

    private Callback callback;

    private boolean isLauncher = false;

    private int width = 30; // 左右边距
    private int height = 300; // 高

    public WebViewDialog(Context context, String url) {
        super(context);
        this.context = context;
        this.url = url;
    }

    public WebViewDialog(Context context, String url, int width, int height) {
        super(context);
        this.context = context;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    public void setCallback(Callback callback) {
        this.callback = callback;
    }

    public void setLauncher(boolean launcher) {
        isLauncher = launcher;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_webview_dialog, null);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setContentView(contentView);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = getScreenWidth((Activity) context) - dp2px(context, width) * 2;
        layoutParams.height = dp2px(context, height);
        window.setAttributes(layoutParams);

        initView();
    }

    private void initView() {
        WebView webView = findViewById(R.id.webView);
        setSettings(webView);
        webView.loadUrl(url);

        findViewById(R.id.btns).setVisibility(isLauncher ? View.VISIBLE : View.GONE);
        findViewById(R.id.refuse).setOnClickListener(v -> {
            if (callback != null) callback.callback(false);
            dismiss();
        });

        findViewById(R.id.agree).setOnClickListener(v -> {
            if (callback != null) callback.callback(true);
            dismiss();
        });
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
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("url", url);
                        context.startActivity(intent);
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

        int uiMode = context.getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
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

        String dir = context.getApplicationContext().getDir("database", Context.MODE_PRIVATE).getPath();
        settings.setGeolocationDatabasePath(dir);
        // 允许Https + Http混用  5.0
        settings.setMixedContentMode(WebSettings.LOAD_DEFAULT);

        webView.addJavascriptInterface(this, "android");
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }

    interface Callback {
        void callback(boolean status);
    }
}
