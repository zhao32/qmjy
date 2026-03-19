package com.cocos.game.csjAdManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Display;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.djx.DJXSdk;
import com.bytedance.sdk.djx.DJXSdkConfig;
import com.bytedance.sdk.djx.DJXToastType;
import com.bytedance.sdk.djx.IDJXPrivacyController;
import com.bytedance.sdk.djx.IDJXService;
import com.bytedance.sdk.djx.IDJXToastController;
import com.bytedance.sdk.djx.model.DJXError;
import com.bytedance.sdk.djx.model.DJXOthers;
import com.bytedance.sdk.djx.model.DJXUser;
import com.bytedance.sdk.nov.api.INovCallback;
import com.bytedance.sdk.nov.api.INovService;
import com.bytedance.sdk.nov.api.NovSdk;
import com.bytedance.sdk.nov.api.NovSdkConfig;
import com.bytedance.sdk.openadsdk.TTAdConfig;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTCustomController;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.Logger;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Random;

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
        TTAdSdk.init(m_ctx, new TTAdConfig.Builder().appId(appId)
                .useMediation(true).debug(true)
                .customController(new TTCustomController() {
                }) // 隐私合规设置
                .build()
        );
        SDK_StartLoad();
    }

    public void SDK_StartLoad() {
        // 开始加载
        TTAdSdk.start(new TTAdSdk.Callback() {
            @Override
            public void success() {
                // 初始化成功
                if (m_isInit) return;
                DebugPrintE("Android: csj 加载SDK成功");
                m_isInit = true;
                // 在初始化成功回调之后进行广告加载
                if (m_adMainCallBack.sdkInitCallBack != null) {
                    m_adMainCallBack.sdkInitCallBack.onSuccess();
                }
                // 添加容器到游戏上下文Activity
                Activity activity = (Activity) m_ctx;
                activity.runOnUiThread(() -> activity.addContentView(CreateAndGetFrameLayout(), getLayoutFull()));
            }

            @Override
            public void fail(int i, String s) {
                // 初始化失败
                DebugPrintE("Android: csj 加载SDK错误 code: %s, msg: %s", i, s);
                m_isInit = false;
                if (m_adMainCallBack.sdkInitCallBack != null) {
                    m_adMainCallBack.sdkInitCallBack.onError(i, s);
                }
            }
        });
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
            int top = 0, height = 0;
            if (data.containsKey("style")) {
                JSONObject style = data.getJSONObject("style");
                if (style.containsKey("top")) top = style.getIntValue("top");
                if (style.containsKey("height")) height = style.getIntValue("height");
            }
            loadAdHandler(AdBanner.getInstance().LoadAd(unitId, height, top), event, (s) -> AdBanner.getInstance().ShowAd(s));
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
            int bottom = 1;
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

    public void handlerDjx(Context context, String event, JSONObject data) {
        if (TextUtils.equals(event, "initDjx")) {
            initDjx(context, event, data);
        } else if (TextUtils.equals(event, "loginDjx")) {
            loginDjx(context, event, data);
        } else if (TextUtils.equals(event, "logoutDjx")) {
            logoutDjx(context, event, data);
        } else if (TextUtils.equals(event, "openDjx")) {
            openDjx(context, event, data);
        }
    }

    private void initDjx(Context context, String event, JSONObject data) {
        // 获取配置文件：https://www.csjplatform.com/supportcenter/28147#gyjjn4wzer40
        // xxx.json
        String sdkSettingFile = data.getString("sdkSettingFile");
        if (!m_isInit || TTAdSdk.getAdManager() == null || TextUtils.isEmpty(sdkSettingFile)) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }

        // 是否新用户
        boolean newUser = data.getBooleanValue("newUser", true);
        // 是否青少年模式，青少年开启后不返回短剧内容
        boolean teenagerMode = data.getBooleanValue("teenagerMode", false);

        // SDK配置说明：https://www.csjplatform.com/supportcenter/28147#1ip1xdqmvleo0
        DJXSdkConfig config = new DJXSdkConfig.Builder()
                .debug(true)
                .newUser(newUser)
                .privacyController(new IDJXPrivacyController() {
                    @Override
                    public boolean isOnlyICPNumber() {
                        // 是否需要只有备案号内容，true仅有备案号、false全量内容(默认)
                        return super.isOnlyICPNumber();
                    }

                    @Override
                    public boolean isTeenagerMode() {
                        // 是否青少年模式，true青少年模式，false正常模式。默认为false，青少年开启后不返回短剧内容
                        return teenagerMode;
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        // 是否允许SDK主动获取手机硬件参数，imei/imsi，true可以获取，false禁止获取。默认为true
                        return super.isCanUsePhoneState();
                    }

                    @Override
                    public String getImei() {
                        return super.getImei();
                    }

                    @Override
                    public String getImsi() {
                        return super.getImsi();
                    }

                    @Override
                    public boolean isCanUseAndroidId() {
                        // 是否允许SDK主动获取android_id参数，true可以获取，false禁止获取。默认为true
                        return super.isCanUseAndroidId();
                    }

                    @Override
                    public String getAndroidId() {
                        return super.getAndroidId();
                    }

                    @Override
                    public boolean isCanUseMac() {
                        return super.isCanUseMac();
                    }

                    @Override
                    public boolean isCanUseOAID() {
                        return super.isCanUseOAID();
                    }

                    @Override
                    public boolean isCanUseICCID() {
                        return super.isCanUseICCID();
                    }

                    @Override
                    public boolean isCanUseSerialNumber() {
                        return super.isCanUseSerialNumber();
                    }

                    @Override
                    public boolean isCanUseGAID() {
                        return super.isCanUseGAID();
                    }

                    @Override
                    public boolean isCanUseOperatorInfo() {
                        return super.isCanUseOperatorInfo();
                    }
                })
                .toastController(new IDJXToastController() {
                    @Override
                    public boolean onToast(Context context, String msg, DJXToastType type) {
                        // true：使用自定义toast；false：使用sdk默认toast
                        return super.onToast(context, msg, type);
                    }
                })
                .build();
        DJXSdk.init(context, sdkSettingFile, config);
        DJXSdk.start(new DJXSdk.StartListener() {
            @Override
            public void onStartComplete(boolean isSuccess, String message, @Nullable DJXError error) {
                DebugPrintE(String.format("短剧 start isSuccess:%s, message:%s, error.code:%s, error.msg:%s",
                        isSuccess, message, error == null ? 0 : error.code, error == null ? "" : error.msg));
                JsbBridgeCallback.getInstance().sendToScript(event, isSuccess ? "1" : "");
            }
        });
    }

    private String getSignDjx(String uid, String sk) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            return "";
        }
        String signString = "";
        IDJXService service = DJXSdk.service();
        if (service != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put("ouid", uid);
            signString = service.getSignString(sk, nonce(), System.currentTimeMillis() / 1000, map);
        }
        return signString;
    }

    private void loginDjx(Context context, String event, JSONObject data) {
        // 签名, 解锁记录绑定
        String sign = data.getString("sign");
        String uid = data.getString("uid");
        String serverKey = data.getString("serverKey");
        DebugPrintE(String.format("loginDjx sign:%s, uid:%s, serverKey:%s", sign, uid, serverKey));
        if (TextUtils.isEmpty(sign) && (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(serverKey))) {
            sign = getSignDjx(uid, serverKey);
            DebugPrintE(String.format("loginDjx getSignDjx:%s", sign));
        }
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess() || TextUtils.isEmpty(sign)) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }
        IDJXService service = DJXSdk.service();
        if (service != null) {
            service.login(sign, new IDJXService.IDJXCallback<DJXUser>() {
                @Override
                public void onSuccess(DJXUser djxUser, @Nullable DJXOthers djxOthers) {
                    DebugPrintE(String.format("短剧 解锁记录绑定 onSuccess userId:%s, ouid:%s, loginType:%s", djxUser.userId, djxUser.ouid, djxUser.loginType));
                    JsbBridgeCallback.getInstance().sendToScript(event, "1");
                }

                @Override
                public void onError(@NonNull DJXError djxError) {
                    DebugPrintE(String.format("短剧 解锁记录绑定 onError code:%s, msg:%s", djxError.code, djxError.msg));
                    JsbBridgeCallback.getInstance().sendToScript(event, "");
                }
            });
        } else JsbBridgeCallback.getInstance().sendToScript(event, "");
    }

    private void logoutDjx(Context context, String event, JSONObject data) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }
        IDJXService service = DJXSdk.service();
        if (service != null) {
            service.logout(new IDJXService.IDJXCallback<DJXUser>() {
                @Override
                public void onSuccess(DJXUser djxUser, @Nullable DJXOthers djxOthers) {
                    DebugPrintE(String.format("短剧 解锁记录解绑 onSuccess userId:%s, ouid:%s, loginType:%s", djxUser.userId, djxUser.ouid, djxUser.loginType));
                    JsbBridgeCallback.getInstance().sendToScript(event, "1");
                }

                @Override
                public void onError(@NonNull DJXError djxError) {
                    DebugPrintE(String.format("短剧 解锁记录解绑 onError code:%s, msg:%s", djxError.code, djxError.msg));
                    JsbBridgeCallback.getInstance().sendToScript(event, "");
                }
            });
        } else JsbBridgeCallback.getInstance().sendToScript(event, "1");
    }

    private void openDjx(Context context, String event, JSONObject data) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }

        String mode = data.getString("mode");
        if (TextUtils.isEmpty(mode)) mode = "home";

        if (TextUtils.equals("home", mode)) {
            // 短剧免费集数
            int freeSet = data.getIntValue("freeSet", 1);
            // 短剧广告解锁集数
            int lockSet = data.getIntValue("lockSet", 1);

            Intent intent = new Intent(context, DjxHomeActivity.class);
            intent.putExtra("freeSet", freeSet);
            intent.putExtra("lockSet", lockSet);
            context.startActivity(intent);
        }
    }

    public void handlerNov(Context context, String event, JSONObject data) {
        if (TextUtils.equals(event, "initNov")) {
            initNov(context, event, data);
        } else if (TextUtils.equals(event, "loginNov")) {
            loginNov(context, event, data);
        } else if (TextUtils.equals(event, "logoutNov")) {
            logoutNov(context, event, data);
        } else if (TextUtils.equals(event, "openNov")) {
            openNov(context, event, data);
        }
    }

    private void initNov(Context context, String event, JSONObject data) {
        // 获取配置文件：https://www.csjplatform.com/supportcenter/28147#gyjjn4wzer40
        // xxx.json
        String sdkSettingFile = data.getString("sdkSettingFile");
        if (!m_isInit || TTAdSdk.getAdManager() == null || TextUtils.isEmpty(sdkSettingFile)) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }

        // 是否新用户
        boolean newUser = data.getBooleanValue("newUser", true);
        // 是否青少年模式，青少年开启后不返回短剧内容
        boolean teenagerMode = data.getBooleanValue("teenagerMode", false);

        // SDK配置说明：https://www.csjplatform.com/supportcenter/28147#1ip1xdqmvleo0
        NovSdkConfig config = new NovSdkConfig.Builder()
                .debug(true)
                .newUser(newUser)
                .privacyController(new IDJXPrivacyController() {
                    @Override
                    public boolean isOnlyICPNumber() {
                        // 是否需要只有备案号内容，true仅有备案号、false全量内容(默认)
                        return super.isOnlyICPNumber();
                    }

                    @Override
                    public boolean isTeenagerMode() {
                        // 是否青少年模式，true青少年模式，false正常模式。默认为false，青少年开启后不返回短剧内容
                        return teenagerMode;
                    }

                    @Override
                    public boolean isCanUsePhoneState() {
                        // 是否允许SDK主动获取手机硬件参数，imei/imsi，true可以获取，false禁止获取。默认为true
                        return super.isCanUsePhoneState();
                    }

                    @Override
                    public String getImei() {
                        return super.getImei();
                    }

                    @Override
                    public String getImsi() {
                        return super.getImsi();
                    }

                    @Override
                    public boolean isCanUseAndroidId() {
                        // 是否允许SDK主动获取android_id参数，true可以获取，false禁止获取。默认为true
                        return super.isCanUseAndroidId();
                    }

                    @Override
                    public String getAndroidId() {
                        return super.getAndroidId();
                    }

                    @Override
                    public boolean isCanUseMac() {
                        return super.isCanUseMac();
                    }

                    @Override
                    public boolean isCanUseOAID() {
                        return super.isCanUseOAID();
                    }

                    @Override
                    public boolean isCanUseICCID() {
                        return super.isCanUseICCID();
                    }

                    @Override
                    public boolean isCanUseSerialNumber() {
                        return super.isCanUseSerialNumber();
                    }

                    @Override
                    public boolean isCanUseGAID() {
                        return super.isCanUseGAID();
                    }

                    @Override
                    public boolean isCanUseOperatorInfo() {
                        return super.isCanUseOperatorInfo();
                    }
                })
                .toastController(new IDJXToastController() {
                    @Override
                    public boolean onToast(Context context, String msg, DJXToastType type) {
                        // true：使用自定义toast；false：使用sdk默认toast
                        return super.onToast(context, msg, type);
                    }
                })
                .build();
        NovSdk.init(context, sdkSettingFile, config);
        NovSdk.start(new NovSdk.StartListener() {
            @Override
            public void onStartComplete(boolean isSuccess, @Nullable String message, @Nullable DJXError error) {
                DebugPrintE(String.format("短故事 start isSuccess:%s, message:%s, error.code:%s, error.msg:%s",
                        isSuccess, message, error == null ? 0 : error.code, error == null ? "" : error.msg));
                JsbBridgeCallback.getInstance().sendToScript(event, isSuccess ? "1" : "");
            }
        });
    }

    private String getSignNov(String uid, String sk) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            return "";
        }
        String signString = "";
        INovService service = NovSdk.service();
        if (service != null) {
            HashMap<String, String> map = new HashMap<>();
            map.put("ouid", uid);
            signString = service.getSignString(sk, nonce(), System.currentTimeMillis() / 1000, map);
        }
        return signString;
    }

    private void loginNov(Context context, String event, JSONObject data) {
        // 签名, 解锁记录绑定
        String sign = data.getString("sign");
        String uid = data.getString("uid");
        String serverKey = data.getString("serverKey");
        DebugPrintE(String.format("loginNov sign:%s, uid:%s, serverKey:%s", sign, uid, serverKey));
        if (TextUtils.isEmpty(sign) && (!TextUtils.isEmpty(uid) && !TextUtils.isEmpty(serverKey))) {
            sign = getSignNov(uid, serverKey);
            DebugPrintE(String.format("loginNov getSignNov:%s", sign));
        }
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess() || TextUtils.isEmpty(sign)) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }
        INovService service = NovSdk.service();
        if (service != null) {
            service.login(sign, new INovCallback<DJXUser>() {
                @Override
                public void onSuccess(DJXUser djxUser, @Nullable DJXOthers djxOthers) {
                    DebugPrintE(String.format("短故事 阅读记录绑定 onSuccess userId:%s, ouid:%s, loginType:%s", djxUser.userId, djxUser.ouid, djxUser.loginType));
                    JsbBridgeCallback.getInstance().sendToScript(event, "1");
                }

                @Override
                public void onError(@NonNull DJXError djxError) {
                    DebugPrintE(String.format("短故事 阅读记录绑定 onError code:%s, msg:%s", djxError.code, djxError.msg));
                    JsbBridgeCallback.getInstance().sendToScript(event, "");
                }
            });
        } else JsbBridgeCallback.getInstance().sendToScript(event, "");
    }

    private void logoutNov(Context context, String event, JSONObject data) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }
        INovService service = NovSdk.service();
        if (service != null) {
            service.logout(new INovCallback<DJXUser>() {
                @Override
                public void onSuccess(DJXUser djxUser, @Nullable DJXOthers djxOthers) {
                    DebugPrintE(String.format("短故事 阅读记录解绑 onSuccess userId:%s, ouid:%s, loginType:%s", djxUser.userId, djxUser.ouid, djxUser.loginType));
                    JsbBridgeCallback.getInstance().sendToScript(event, "1");
                }

                @Override
                public void onError(@NonNull DJXError djxError) {
                    DebugPrintE(String.format("短故事 阅读记录解绑 onError code:%s, msg:%s", djxError.code, djxError.msg));
                    JsbBridgeCallback.getInstance().sendToScript(event, "");
                }
            });
        } else JsbBridgeCallback.getInstance().sendToScript(event, "1");
    }

    private void openNov(Context context, String event, JSONObject data) {
        if (!m_isInit || TTAdSdk.getAdManager() == null || !DJXSdk.isStartSuccess()) {
            JsbBridgeCallback.getInstance().sendToScript(event, "");
            return;
        }

        String mode = data.getString("mode");
        if (TextUtils.isEmpty(mode)) mode = "home";

        if (TextUtils.equals("home", mode)) {
            // 文末推荐页推荐个数
            int endPageRecSize = data.getIntValue("endPageRecSize", 3);
            // 阅读器默认翻页模式
            String pageTurnMode = data.getString("pageTurnMode");

            Intent intent = new Intent(context, NovHomeActivity.class);
            intent.putExtra("endPageRecSize", endPageRecSize);
            intent.putExtra("pageTurnMode", pageTurnMode);
            context.startActivity(intent);
        }
    }

    private String nonce() {
        int length = 16;
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }
}
