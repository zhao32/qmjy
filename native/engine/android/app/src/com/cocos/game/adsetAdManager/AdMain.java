package com.cocos.game.adsetAdManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.Logger;
import com.cocos.game.adManager.AdManager;
import com.kc.openset.config.ChannelType;
import com.kc.openset.config.OSETSDK;
import com.kc.openset.listener.OSETInitListener;

public class AdMain {
    @SuppressLint("StaticFieldLeak")
    private static AdMain instance;
    private Context m_ctx;
    private final AdMainCallBack m_adMainCallBack = new AdMainCallBack();
    private boolean m_isInit;
    private boolean m_debugLog;
    private FrameLayout m_frameLayout;

    public static AdMain getInstance() {
        if (instance == null) {
            instance = new AdMain();
        }
        return instance;
    }

    public void setGameCtx(Context ctx) {
        this.m_ctx = ctx;
    }

    public Context getGameCtx() {
        return m_ctx;
    }

    public void setAdMainCallBack(AdMainCallBack.SDKInitCallBack sdkInitCallBack) {
        m_adMainCallBack.Handler(sdkInitCallBack);
    }

    public void setDebugLogEnable(boolean enable) {
        this.m_debugLog = enable;
    }

    public void DebugPrintE(String format, Object... args) {
        if (m_debugLog) {
            Logger.e("", String.format(format, args));
        }
    }

    public FrameLayout CreateAndGetFrameLayout() {
        if (m_frameLayout == null) {
            m_frameLayout = new FrameLayout(m_ctx);
        }
        return m_frameLayout;
    }

    public FrameLayout getMainView() {
        return m_frameLayout;
    }

    public FrameLayout.LayoutParams getLayoutFull() {
        FrameLayout.LayoutParams lytp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lytp.gravity = Gravity.CENTER;
        return lytp;
    }

    public Point getScreen() {
        WindowManager wm = ((WindowManager) m_ctx.getSystemService(Context.WINDOW_SERVICE));
        Display display;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        } else {
            DebugPrintE("getSystemService wm = null");
            return null;
        }

        Point screenSize = new Point();
        if (display != null) {
            display.getRealSize(screenSize);
        } else {
            DebugPrintE("getSystemService display = null");
            return null;
        }
        DebugPrintE("getscreenSize xy：" + screenSize.x + " " + screenSize.y);
        return screenSize;
    }

    public void SDK_Init(String appId) {
        if (m_isInit) {
            return;
        }
        if (m_ctx == null) {
            return;
        }
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        OSETSDK.getInstance()
                .setChannel(ChannelType.DEFAULT)
                .setUserId(getProcessName(m_ctx))
                .init((Application) m_ctx.getApplicationContext(), appId, new OSETInitListener() {
                    @Override
                    public void onSuccess() {
                        if (m_isInit) return;
                        DebugPrintE("Android: adset 加载SDK成功");
                        m_isInit = true;
                        SDK_StartLoad();
                        if (m_adMainCallBack.sdkInitCallBack != null) {
                            m_adMainCallBack.sdkInitCallBack.onSuccess();
                        }
                        // 添加容器到游戏上下文Activity
                        Activity activity = (Activity) m_ctx;
                        activity.runOnUiThread(() -> activity.addContentView(CreateAndGetFrameLayout(), getLayoutFull()));
                    }

                    @Override
                    public void onError(String s) {
                        DebugPrintE("Android: adset 加载SDK错误 msg: %s", s);
                        m_isInit = false;
                        if (m_adMainCallBack.sdkInitCallBack != null) {
                            m_adMainCallBack.sdkInitCallBack.onError(0, s);
                        }
                    }
                });
    }

    public void SDK_StartLoad() {
    }

    public void handlerAd(Context context, String event, JSONObject data) {
        if (TextUtils.equals(event, "initAd")) {
            String appId = data.getString("appId");
            initAdHandler(context, event, appId);
        } else if (TextUtils.equals(event, "splashAd")) {
            String unitId = data.getString("unitId");
            loadAdHandler(AdSplash.getInstance().LoadAd(unitId), event, (s) -> AdSplash.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "bannerAd")) {
            String unitId = data.getString("unitId");
            int top = 0;
            if (data.containsKey("style")) {
                JSONObject style = data.getJSONObject("style");
                if (style.containsKey("top")) top = style.getIntValue("top");
            }
            loadAdHandler(AdBanner.getInstance().LoadAd(unitId, top), event, (s) -> AdBanner.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "interstitialAd")) {
            String unitId = data.getString("unitId");
            loadAdHandler(AdInterstitial.getInstance().LoadAd(unitId), event, (s) -> AdInterstitial.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "rewardedVideoAd")) {
            String unitId = data.getString("unitId");
            loadAdHandler(AdReward.getInstance().LoadAd(unitId), event, (s) -> AdReward.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "feedAd")) {
            String unitId = data.getString("unitId");
            int left = 0;
            int bottom = 0;
            if (data.containsKey("style")) {
                JSONObject style = data.getJSONObject("style");
                if (style.containsKey("left")) left = style.getIntValue("left");
                if (style.containsKey("bottom")) bottom = style.getIntValue("bottom");
            }
            loadAdHandler(AdFeed.getInstance().LoadAd(unitId, left, bottom), event, (s) -> AdFeed.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "drawAd")) {
            String unitId = data.getString("unitId");
            int bottom = 0;
            if (data.containsKey("style")) {
                JSONObject style = data.getJSONObject("style");
                if (style.containsKey("bottom")) bottom = style.getIntValue("bottom");
            }
            loadAdHandler(AdDraw.getInstance().LoadAd(unitId, bottom), event, (s) -> AdDraw.getInstance().ShowAd(s));
        }
    }

    private void initAdHandler(Context context, String event, String appId) {
        AdMain adManager = AdMain.getInstance();
        adManager.setDebugLogEnable(true);
        adManager.setGameCtx(context);
        adManager.setAdMainCallBack(new AdMainCallBack.SDKInitCallBack() {
            @Override
            public void onSuccess() {
                JsbBridgeCallback.getInstance().sendToScript(event, "1");
            }

            @Override
            public void onError(int i, String e) {
                JsbBridgeCallback.getInstance().sendToScript(event, "");
            }
        });
        adManager.SDK_Init(appId);
    }

    private void loadAdHandler(AdMainCallBack callBack, String event, ShowAd showAd) {
        callBack.Handler(new AdMainCallBack.AdLoadStatusCallBack() {
            @Override
            public void onSuccess(AdMainCallBack.LoadStatusType type, Object obj) {
                if (type == AdMainCallBack.LoadStatusType.RENDER) {
                    String str = String.valueOf(obj);
                    if (!TextUtils.equals("2", str)) {
                        showAd.showAd(str);
                        if (TextUtils.equals(event, "splashAd"))
                            AdManager.getInstance().setSplashState(1);
                        if (TextUtils.equals(event, "feedAd"))
                            AdManager.getInstance().setFeedState(1);
                        if (TextUtils.equals(event, "bannerAd"))
                            AdManager.getInstance().setBannerState(1);
                        if (TextUtils.equals(event, "rewardedVideoAd"))
                            AdManager.getInstance().setRewardState(1);
                        if (TextUtils.equals(event, "interstitialAd"))
                            AdManager.getInstance().setInterstitialState(1);
                        if (TextUtils.equals(event, "drawAd"))
                            AdManager.getInstance().setDrawState(1);
                    }
                    JsbBridgeCallback.getInstance().sendToScript(event, obj == null ? "1" : String.valueOf(obj));
                }
            }

            @Override
            public void onError(AdMainCallBack.LoadStatusType type, Object obj, int i, String e) {
                if (type == AdMainCallBack.LoadStatusType.LOAD || type == AdMainCallBack.LoadStatusType.RENDER) {
                    JsbBridgeCallback.getInstance().sendToScript(event, "");
                }
            }
        });
    }

    public interface ShowAd {
        void showAd(String data);
    }

    private String getProcessName(Context context) {
        int pid = android.os.Process.myPid();
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningAppProcessInfo processInfo : manager.getRunningAppProcesses()) {
            if (processInfo.pid == pid) {
                return processInfo.processName;
            }
        }
        return null;
    }
}
