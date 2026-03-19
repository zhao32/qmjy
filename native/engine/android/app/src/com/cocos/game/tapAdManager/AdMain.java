package com.cocos.game.tapAdManager;

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
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.Logger;
import com.cocos.game.adManager.AdManager;
import com.tapsdk.tapad.CustomUser;
import com.tapsdk.tapad.TapAdConfig;
import com.tapsdk.tapad.TapAdCustomController;
import com.tapsdk.tapad.TapAdLocation;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdSdk;
import com.yourong.game.jydhm.R;

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
        DebugPrintE("getscreenSize xy：" + screenSize.x + " " + screenSize.y);
        return screenSize;
    }

    public float px2dp(float px) {
        final float scale = m_ctx.getResources().getDisplayMetrics().density;
        return px / Math.max(1, scale) + 0.5f;
    }

    public float dp2px(float dp) {
        final float scale = m_ctx.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }

    /***
     * SDK 初始化
     */
    public void SDK_Init(String appId, String appKey, String appName, String appChannel, String appClientID) {
        if (m_isInit) {
            return;
        }
        if (m_ctx == null) {
            return;
        }
        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(appKey)) {
            return;
        }
        if (TextUtils.isEmpty(appName)) appName = m_ctx.getString(R.string.app_name);
        if (TextUtils.isEmpty(appChannel)) appChannel = "taptap";
        // TapAdManager.get().requestPermissionIfNecessary(m_ctx);
        TapAdConfig config = new TapAdConfig.Builder()
                // 必选参数。为 TapADN 注册的媒体 ID
                .withMediaId(Long.parseLong(appId))
                // 必选参数。媒体密钥，可以在TapADN后台查看
                .withMediaKey(appKey)
                // 必选参数。为 TapADN 注册的媒体名称
                .withMediaName(appName)
                // 必选参数，渠道（如果在 TapTap 上架填写 "taptap", 其它渠道上架填写商店拼音小写字母，eg. 小米 -> "xiaomi"）
                .withGameChannel(appChannel)
                // 必选参数。默认值 "1"
                .withMediaVersion("1")
                // 可选参数。TapTap 开发者中心的游戏 Client ID
                .withTapClientId(appClientID)
                // 可选参数，是否开启摇一摇: true 打开、false 关闭。默认 true 开启
                .shakeEnabled(false)
                // 可选参数，是否打开 debug 调试信息输出：true 打开、false 关闭。默认 false 关闭
                .enableDebug(true)
                .withCustomController(new TapAdCustomController() {
                    @Override
                    public CustomUser provideCustomUser() {
                        return super.provideCustomUser();
                    }

                    @Override
                    public boolean isCanUseAndroidId() {
                        return super.isCanUseAndroidId();
                    }

                    @Override
                    public boolean alist() {
                        return super.alist();
                    }

                    @Override
                    public String getDevOaid() {
                        return super.getDevOaid();
                    }

                    @Override
                    public boolean isCanUseWriteExternal() {
                        return super.isCanUseWriteExternal();
                    }

                    @Override
                    public boolean isCanUseWifiState() {
                        return super.isCanUseWifiState();
                    }

                    @Override
                    public String getDevImei() {
                        return super.getDevImei();
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        return super.isCanUsePhoneState();
                    }

                    @Override
                    public TapAdLocation getTapAdLocation() {
                        return super.getTapAdLocation();
                    }

                    @Override
                    public boolean isCanUseLocation() {
                        return super.isCanUseLocation();
                    }
                })
                .build();
        TapAdSdk.init(m_ctx, config);
        SDK_StartLoad();
    }

    public void SDK_StartLoad() {
        DebugPrintE("Android: tap 加载SDK成功");
        // 初始化成功
        m_isInit = true;
        // 在初始化成功回调之后进行广告加载
        if (m_adMainCallBack.sdkInitCallBack != null) {
            m_adMainCallBack.sdkInitCallBack.onSuccess();
        }
        // 添加容器到游戏上下文Activity
        Activity activity = (Activity) m_ctx;
        activity.runOnUiThread(() -> activity.addContentView(CreateAndGetFrameLayout(), getLayoutFull()));
    }

    public void handlerAd(Context context, String event, JSONObject data) {
        if (TextUtils.equals(event, "initAd")) {
            String appId = data.getString("appId");
            String appKey = data.getString("appKey");
            String appName = data.getString("appName");
            String appChannel = data.getString("appChannel");
            String appClientID = data.getString("appClientID");
            initAdHandler(context, event, appId, appKey, appName, appChannel, appClientID);
        } else if (TextUtils.equals(event, "splashAd")) {
            String unitId = data.getString("unitId");
            String userId = data.getString("userId");
            loadAdHandler(AdSplash.getInstance().LoadAd(unitId, userId), event, (s) -> AdSplash.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "bannerAd")) {
            String unitId = data.getString("unitId");
            String userId = data.getString("userId");
            int top = 0;
            if (data.containsKey("style")) {
                JSONObject style = data.getJSONObject("style");
                if (style.containsKey("top")) top = style.getIntValue("top");
            }
            loadAdHandler(AdBanner.getInstance().LoadAd(unitId, userId, top), event, (s) -> AdBanner.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "interstitialAd")) {
            String unitId = data.getString("unitId");
            String userId = data.getString("userId");
            loadAdHandler(AdInterstitial.getInstance().LoadAd(unitId, userId), event, (s) -> AdInterstitial.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "rewardedVideoAd")) {
            String unitId = data.getString("unitId");
            String userId = data.getString("userId");
            loadAdHandler(AdReward.getInstance().LoadAd(unitId, userId), event, (s) -> AdReward.getInstance().ShowAd(s));
        } else if (TextUtils.equals(event, "feedAd")) {
            DebugPrintE("Android: tap 不支持 %s", "feed");
        } else if (TextUtils.equals(event, "drawAd")) {
            DebugPrintE("Android: tap 不支持 %s", "draw");
        }
    }

    private void initAdHandler(Context context, String event,
                               String appId, String appKey, String appName, String appChannel, String appClientID) {
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
        adManager.SDK_Init(appId, appKey, appName, appChannel, appClientID);
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
