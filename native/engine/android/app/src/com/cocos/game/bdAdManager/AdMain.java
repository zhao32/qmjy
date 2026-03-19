package com.cocos.game.bdAdManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSONObject;
import com.baidu.mobads.sdk.api.BDAdConfig;
import com.baidu.mobads.sdk.api.BDDialogParams;
import com.baidu.mobads.sdk.api.MobadsPermissionSettings;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.Logger;
import com.cocos.game.adManager.AdManager;

public class AdMain {
    @SuppressLint("StaticFieldLeak")
    private static AdMain instance;
    private Context m_ctx;
    private final AdMainCallBack m_adMainCallBack = new AdMainCallBack();
    private boolean m_isInit; // 是否初始化
    private boolean m_debugLog;
    private FrameLayout m_frameLayout;

    // 获取&创建单例类
    public static AdMain getInstance() {
        if (instance == null) {
            instance = new AdMain();
        }
        return instance;
    }

    /***
     * 设置游戏主类上下文
     * @param ctx 主类上下文
     */
    public void setGameCtx(Context ctx) {
        this.m_ctx = ctx;
    }

    public Context getGameCtx() {
        return m_ctx;
    }

    public void setAdMainCallBack(AdMainCallBack.SDKInitCallBack sdkInitCallBack) {
        m_adMainCallBack.Handler(sdkInitCallBack);
    }

    /***
     * 是否开启 调试日志输出
     * @param enable 调试输出开关
     */
    public void setDebugLogEnable(boolean enable) {
        this.m_debugLog = enable;
    }

    public void DebugPrintE(String format, Object... args) {
        if (m_debugLog) {
            Logger.e("", String.format(format, args));
        }
    }

    /***
     * 创建一个FrameLayout布局
     * @return 拿到这个布局
     */
    public FrameLayout CreateAndGetFrameLayout() {
        if (m_frameLayout == null) {
            m_frameLayout = new FrameLayout(m_ctx);
        }
        return m_frameLayout;
    }

    public FrameLayout getMainView() {
        return m_frameLayout;
    }

    /***
     * 获取一个撑满全屏的布局参数
     * @return 返回此参数
     */
    public FrameLayout.LayoutParams getLayoutFull() {
        FrameLayout.LayoutParams lytp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        lytp.gravity = Gravity.CENTER;
        return lytp;
    }

    /***
     * 获取屏幕真实宽高
     * @return 二维对象
     */
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
        DebugPrintE("getscreenSize: w * h：" + screenSize.x + " * " + screenSize.y);
        return screenSize;
    }

    /***
     * SDK 初始化
     */
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
        BDAdConfig config = new BDAdConfig.Builder()
                .setAppsid(appId).setDebug(true)
                .setDialogParams(
                        new BDDialogParams.Builder()
                                .setDlDialogType(BDDialogParams.TYPE_BOTTOM_POPUP)
                                .setDlDialogAnimStyle(BDDialogParams.ANIM_STYLE_NONE)
                                .build()
                )
                .setBDAdInitListener(new BDAdConfig.BDAdInitListener() {
                    @Override
                    public void success() {
                        // 初始化成功
                        if (m_isInit) return;
                        DebugPrintE("Android: bd 加载SDK成功");
                        m_isInit = true;
                        SDK_StartLoad();
                        // 在初始化成功回调之后进行广告加载
                        if (m_adMainCallBack.sdkInitCallBack != null) {
                            m_adMainCallBack.sdkInitCallBack.onSuccess();
                        }
                        // 添加容器到游戏上下文Activity
                        Activity activity = (Activity) m_ctx;
                        activity.runOnUiThread(() -> activity.addContentView(CreateAndGetFrameLayout(), getLayoutFull()));
                    }

                    @Override
                    public void fail() {
                        // 初始化失败
                        DebugPrintE("Android: bd 加载SDK错误");
                        m_isInit = false;
                        if (m_adMainCallBack.sdkInitCallBack != null) {
                            m_adMainCallBack.sdkInitCallBack.onError(0, "");
                        }
                    }
                }).build(m_ctx);
        config.preInit();
        config.init();
        MobadsPermissionSettings.setPermissionDeviceInfo(true);
        MobadsPermissionSettings.setPermissionReadDeviceID(true);
        MobadsPermissionSettings.setPermissionOAID(true);
        MobadsPermissionSettings.setPermissionStorage(true);
        MobadsPermissionSettings.setPermissionLocation(true);
        MobadsPermissionSettings.setPermissionAppList(true);
        MobadsPermissionSettings.setPermissionAppUpdate(true);
        MobadsPermissionSettings.setPermissionRunningApp(true);
    }

    public void SDK_StartLoad() {
    }

    public void handlerAd(Context context, String event, JSONObject data) {
        if (TextUtils.equals(event, "initAd")) {
            String appId = data.getString("appId");
            initAdHandler(context, event, appId);
        } else if (TextUtils.equals(event, "splashAd")) {
            String unitId = data.getString("unitId");
            loadAdHandler(AdSplash.getInstance().LoadAd(unitId), event, (s) -> AdSplash.getInstance().ShowAd());
        } else if (TextUtils.equals(event, "bannerAd")) {
            DebugPrintE("Android: bd 不支持 %s", "banner");
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
            DebugPrintE("Android: bd 不支持 %s", "draw");
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

}
