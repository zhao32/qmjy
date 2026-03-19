package com.cocos.game;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.mediation.IMediationManager;
import com.bytedance.sdk.openadsdk.mediation.bridge.MediationValueSetBuilder;
import com.chrishui.snackbar.ToastKt;
import com.cocos.game.adManager.AdManager;
import com.cocos.game.iqyAdManager.QiLin;
import com.cocos.lib.CocosActivity;
import com.cocos.lib.CocosHelper;
import com.cocos.lib.CocosJavascriptJavaBridge;
import com.cocos.lib.JsbBridge;
import com.fm.openinstall.OpenInstall;
import com.fm.openinstall.listener.AppInstallAdapter;
import com.fm.openinstall.model.AppData;
import com.lahm.library.EasyProtectorLib;
import com.umeng.sdk.UMengUtil;
import com.yourong.game.jydhm.R;

import org.json.JSONException;

import java.lang.reflect.Method;
import java.util.List;

public class JsbBridgeCallback {
    private JsbBridgeCallback() {
    }

    private static final class InstanceHolder {
        private static final JsbBridgeCallback mInstance = new JsbBridgeCallback();
    }

    public static JsbBridgeCallback getInstance() {
        return InstanceHolder.mInstance;
    }

    public void onScript(String req, String arg) {
        if (mCallback != null) mCallback.onScript(req, arg);
    }

    public void sendToScript(String event, String data) {
        JSONObject obj = new JSONObject();
        obj.put("event", event);
        obj.put("data", data);
        String resp = JSON.toJSONString(obj);
        Logger.d("", String.format("Android: app sendToScript %s", resp));

        JsbBridge.sendToScript(resp);

        CocosHelper.runOnGameThread(() -> {
            String value = String.format("window.onNative(%s,'%s')", obj, "");
            CocosJavascriptJavaBridge.evalString(value);
        });
    }

    protected void initOaid(Context context) {
        UMengUtil.getInstance().getOaid(context, value -> {
            Logger.d("", String.format("initOaid %s", value));
            SharedPreferences game = context.getSharedPreferences("game", Context.MODE_PRIVATE);
            game.edit().putString("oaid", value).apply();
        });
    }

    protected String getOaid(Context context) {
        SharedPreferences game = context.getSharedPreferences("game", Context.MODE_PRIVATE);
        String oaid = game.getString("oaid", "");
        if (!TextUtils.isEmpty(oaid)) return oaid;

        if (TTAdSdk.isSdkReady()) {
            MediationValueSetBuilder valueSet = MediationValueSetBuilder.create();
            valueSet.add(1004, context);
            IMediationManager manager = TTAdSdk.getMediationManager();
            return manager != null ? (String) manager.mtool(1007, valueSet.build()) : "";
        } else return "";
    }

    public interface ICallback {
        void onScript(String req, String arg);
    }

    private CocosActivity mContext;
    private ICallback mCallback;

    public void init(CocosActivity context) {
        mContext = context;
        initOaid(context);
        LoadingDialog.getInstance().init(context);

        // oppo game --> 防沉迷服务
        String oppoSecret = context.getString(R.string.oppo_game_app_secret);
        if (!TextUtils.isEmpty(oppoSecret)) {
            context.runOnUiThread(() -> FcmUtil.getInstance().initOppoGame(context, oppoSecret, false));
        }
        // YSdk --> 防沉迷服务
        boolean ySdkFirst = Boolean.parseBoolean(context.getString(R.string.ysdk_first));
        if (ySdkFirst) {
            context.runOnUiThread(() -> FcmUtil.getInstance().initYSdk(context, false));
        }
        // taptap --> 防沉迷服务
        String taptapId = context.getString(R.string.taptap_gameid);
        String taptapToken = context.getString(R.string.taptap_gametoken);
        if (!TextUtils.isEmpty(taptapId) && !TextUtils.isEmpty(taptapToken)) {
            context.runOnUiThread(() -> FcmUtil.getInstance().initTaptap(context, taptapId, taptapToken));
        }
        // 好游快爆 --> 防沉迷服务
        String hykbId = context.getString(R.string.hykb_gameid);
        if (!TextUtils.isEmpty(hykbId)) {
            context.runOnUiThread(() -> FcmUtil.getInstance().initHykb(context, hykbId));
        }
        // m4399 --> 防沉迷服务
        String m4399Id = context.getString(R.string.m4399_gameid);
        if (!TextUtils.isEmpty(m4399Id)) {
            context.runOnUiThread(() -> FcmUtil.getInstance().initM4399(context, m4399Id));
        }

        // OpenInstall --> 安装参数
        String openInstallKey = context.getString(R.string.openinstall_key);
        if (!TextUtils.isEmpty(openInstallKey)) {
            OpenInstall.clipBoardEnabled(false);
            OpenInstall.preInit(context);
            OpenInstall.init(context, openInstallKey);
        }

        mCallback = (req, arg) -> {
            Logger.d("", String.format("Android: app onScript %s", req));
            context.runOnUiThread(() -> {
                JSONObject requ = JSON.parseObject(req);
                String event = requ.getString("event");
                JSONObject data = requ.getJSONObject("data");
                if (TextUtils.equals(event, "initUM")) {
                    String uAppId = data.getString("umid");
                    String wxAppId = data.getString("wx");
                    String wxAppSecret = data.getString("wxs");
                    String qqAppId = data.getString("qq");
                    String qqAppSecret = data.getString("qqs");
                    String sinaAppId = data.getString("sina");
                    String sinaAppSecret = data.getString("sinas");
                    UMengUtil.getInstance().init(context, uAppId, wxAppId, wxAppSecret, qqAppId, qqAppSecret, sinaAppId, sinaAppSecret);
                } else if (TextUtils.equals(event, "login")) {
                    String provider = data.getString("provider");
                    UMengUtil.getInstance().login(provider, oauth -> sendToScript(event, oauth));
                } else if (TextUtils.equals(event, "share")) {
                    String provider = data.getString("provider");
                    int type = data.getIntValue("type");
                    if (type == 3) {
                        String avatar = data.getString("avatar");
                        String inviteCode = data.getString("inviteCode");
                        String nickname = data.getString("nickname");
                        String content = data.getString("content");
                        QRCodeFullscreenDialog qrCodeDialog = new QRCodeFullscreenDialog(context, provider, avatar, inviteCode, nickname, content);
                        qrCodeDialog.setCanceledOnTouchOutside(true);
                        qrCodeDialog.setCancelable(true);
                        DialogUtil.configSystemUI(context, qrCodeDialog);
                        qrCodeDialog.show();
                    } else {
                        String href = data.getString("href");
                        String title = data.getString("title");
                        String summary = data.getString("summary");
                        String image = data.getString("image");
                        UMengUtil.getInstance().share(provider, type, href, title, summary, image, share -> sendToScript(event, share));
                    }
                } else if (TextUtils.equals(event, "payment")) {
                    String provider = data.getString("provider");
                    String orderInfo = data.getString("data");
                    if (TextUtils.equals(provider, "wxpay")) {
                        PayUtil.wxpay(orderInfo);
                    } else if (TextUtils.equals(provider, "alipay")) {
                        PayUtil.alipay(orderInfo);
                    }
                } else if (TextUtils.equals(event, "requestMerchantTransfer")) {
                    String mchId = data.getString("mchId");
                    String appId = data.getString("appId");
                    String pkg = data.getString("package");
                    PayUtil.requestMerchantTransfer(context, mchId, appId, pkg);
                } else if (TextUtils.equals(event, "initAd") ||
                        TextUtils.equals(event, "feedAd") ||
                        TextUtils.equals(event, "splashAd") ||
                        TextUtils.equals(event, "bannerAd") ||
                        TextUtils.equals(event, "interstitialAd") ||
                        TextUtils.equals(event, "rewardedVideoAd") ||
                        TextUtils.equals(event, "drawAd")) {
                    AdManager.getInstance().reset();
                    String provider = data.getString("provider");
                    if (TextUtils.equals(provider, "bd")) {
                        com.cocos.game.bdAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "csj")) {
                        com.cocos.game.csjAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "gdt")) {
                        com.cocos.game.gdtAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "sigmob")) {
                        com.cocos.game.sigmobAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "sigmobMill")) {
                        com.cocos.game.sigmobMillAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "ks")) {
                        com.cocos.game.ksAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "iqy")) {
                        com.cocos.game.iqyAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "adset") || TextUtils.equals(provider, "openset")) {
                        com.cocos.game.adsetAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "taku")) {
                        com.cocos.game.takuAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "oppo")) {
                        com.cocos.game.oppoAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    } else if (TextUtils.equals(provider, "tap")) {
                        com.cocos.game.tapAdManager.AdMain.getInstance().handlerAd(context, event, data);
                    }
                } else if (TextUtils.equals(event, "multipleAd")) {
                    AdManager.getInstance().reset();
                    String unitIds = data.getString("unitIds");
                    List<JSONObject> units = JSON.parseArray(unitIds, JSONObject.class);
                    for (JSONObject unit : units) {
                        String provider = unit.getString("provider");
                        String eventMultiple = unit.getString("event");
                        if (TextUtils.equals(provider, "bd")) {
                            com.cocos.game.bdAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "csj")) {
                            com.cocos.game.csjAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "gdt")) {
                            com.cocos.game.gdtAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "sigmob")) {
                            com.cocos.game.sigmobAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "ks")) {
                            com.cocos.game.ksAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "iqy")) {
                            com.cocos.game.iqyAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "adset") || TextUtils.equals(provider, "openset")) {
                            com.cocos.game.adsetAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "taku")) {
                            com.cocos.game.takuAdManager.AdMain.getInstance().handlerAd(context, eventMultiple, unit);
                        } else if (TextUtils.equals(provider, "oppo")) {
                            com.cocos.game.oppoAdManager.AdMain.getInstance().handlerAd(context, event, data);
                        } else if (TextUtils.equals(provider, "tap")) {
                            com.cocos.game.tapAdManager.AdMain.getInstance().handlerAd(context, event, data);
                        }
                    }
                } else if (TextUtils.equals(event, "initDjx") ||
                        TextUtils.equals(event, "loginDjx") ||
                        TextUtils.equals(event, "logoutDjx") ||
                        TextUtils.equals(event, "openDjx")) {
                    String provider = data.getString("provider");
                    if (TextUtils.equals(provider, "csj")) {
                        com.cocos.game.csjAdManager.AdMain.getInstance().handlerDjx(context, event, data);
                    }
                } else if (TextUtils.equals(event, "initNov") ||
                        TextUtils.equals(event, "loginNov") ||
                        TextUtils.equals(event, "logoutNov") ||
                        TextUtils.equals(event, "openNov")) {
                    String provider = data.getString("provider");
                    if (TextUtils.equals(provider, "csj")) {
                        com.cocos.game.csjAdManager.AdMain.getInstance().handlerNov(context, event, data);
                    }
                } else if (TextUtils.equals(event, "initFcm")) {
                    String provider = data.getString("provider");
                    if (TextUtils.equals(provider, "oppo")) {
                        // oppo game --> 防沉迷服务
                        String appSecret = data.getString("appSecret");
                        boolean login = data.getBooleanValue("login", false);
                        context.runOnUiThread(() -> FcmUtil.getInstance().initOppoGame(context, appSecret, login));
                    } else if (TextUtils.equals(provider, "ysdk")) {
                        // YSdk --> 防沉迷服务
                        boolean login = data.getBooleanValue("login", false);
                        FcmUtil.getInstance().initYSdk(context, login);
                    } else if (TextUtils.equals(provider, "taptap")) {
                        // taptap --> 防沉迷服务
                        String appId = data.getString("appId");
                        String appToken = data.getString("appToken");
                        if (!TextUtils.isEmpty(appId) && !TextUtils.isEmpty(appToken)) {
                            FcmUtil.getInstance().initTaptap(context, appId, appToken);
                        }
                    } else if (TextUtils.equals(provider, "hykb")) {
                        // 好游快爆 --> 防沉迷服务
                        String appId = data.getString("appId");
                        if (!TextUtils.isEmpty(appId)) {
                            FcmUtil.getInstance().initHykb(context, appId);
                        }
                    } else if (TextUtils.equals(provider, "m4399")) {
                        // m4399 --> 防沉迷服务
                        String appId = data.getString("appId");
                        if (!TextUtils.isEmpty(appId)) {
                            FcmUtil.getInstance().initM4399(context, appId);
                        }
                    }
                } else if (TextUtils.equals(event, "initVolcengine")) {
                    // volcengine --> 火山引擎安全SDK
                    String appId = data.getString("appId");
                    String license = data.getString("license");
                    String channel = data.getString("channel");
                    String scene = data.getString("scene");
                    VolcengineUtil.getInstance().initVolcengine(context.getApplication(), appId, license, channel, scene);
                } else if (TextUtils.equals(event, "chooseMedia")) {
                    String mediaType = data.getString("mediaType");
                    String sourceType = data.getString("sourceType");

                    boolean useOss = data.getBooleanValue("useOss");
                    String ak = data.getString("ak");
                    String sk = data.getString("sk");
                    String endpoint = data.getString("endpoint");
                    String bucket = data.getString("bucket");

                    String url = data.getString("url");
                    String fileKey = data.getString("fileKey");
                    String tokenKey = data.getString("tokenKey");
                    String token = data.getString("token");

                    if (TextUtils.equals(sourceType, "album")) {
                        MXPickerUtil.INSTANCE.openAlbum(useOss, ak, sk, endpoint, bucket, mediaType, url, fileKey, tokenKey, token);
                    } else if (TextUtils.equals(sourceType, "camera")) {
                        MXPickerUtil.INSTANCE.openCamera(useOss, ak, sk, endpoint, bucket, mediaType, url, fileKey, tokenKey, token);
                    }
                } else if (TextUtils.equals(event, "saveImage")) {
                    String image = data.getString("image");
                    FileUtil.saveImage(context, image);
                } else if (TextUtils.equals(event, "makePhoneCall")) {
                    MakePhoneCallUtil.getInstance().handler(context, event, data);
                } else if (TextUtils.equals(event, "openWebView")) {
                    boolean external = data.getBooleanValue("external");
                    boolean blank = data.getBooleanValue("blank");
                    String url = data.getString("url");
                    if (external) {
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        context.startActivity(intent);
                    } else if (blank) {
                        String title = data.getString("title");
                        Intent intent = new Intent(context, WebViewActivity.class);
                        intent.putExtra("url", url);
                        intent.putExtra("title", title);
                        context.startActivity(intent);
                    } else {
                        WebViewDialog policyDialog = new WebViewDialog(context, url);
                        policyDialog.setCanceledOnTouchOutside(true);
                        policyDialog.setCancelable(true);
                        DialogUtil.configSystemUI(context, policyDialog);
                        policyDialog.show();
                    }
                } else if (TextUtils.equals(event, "exit")) {
                    if (FcmUtil.getInstance().isInitedOppoGame()) {
                        FcmUtil.getInstance().exitOppoGame(context);
                    } else {
                        context.finish();
                        QuitUtil.exitApp(context);
                    }
                } else if (TextUtils.equals(event, "showToast")) {
                    String text = data.getString("text");
                    if (!TextUtils.isEmpty(text))
                        ToastKt.make(context, text, ToastKt.LENGTH_LONG).show();
                } else if (TextUtils.equals(event, "scanCode")) {
                    ScanCodeUtil.getInstance().handler(context, event, data);
                } else if (TextUtils.equals(event, "showQRCode")) {
                    String href = data.getString("href");
                    if (!TextUtils.isEmpty(href)) {
                        QRCodeDialog qrCodeDialog = new QRCodeDialog(context, href);
                        qrCodeDialog.setCanceledOnTouchOutside(true);
                        qrCodeDialog.setCancelable(true);
                        DialogUtil.configSystemUI(context, qrCodeDialog);
                        qrCodeDialog.show();
                    }
                } else if (TextUtils.equals(event, "setClipboardData")) {
                    String preLabel = data.getString("label");
                    String label = !TextUtils.isEmpty(preLabel) ? preLabel : "label";
                    String text = data.getString("text");
                    if (!TextUtils.isEmpty(text)) {
                        ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                        ClipData clipData = ClipData.newPlainText(label, text);
                        clipBoard.setPrimaryClip(clipData);
                        ToastKt.make(context, "已复制", ToastKt.LENGTH_LONG).show();
                    }
                } else if (TextUtils.equals(event, "getClipboardData")) {
                    ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = clipBoard.getPrimaryClip();
                    if (clipData != null && clipData.getItemCount() > 0) {
                        ClipData.Item clipDataItem = clipData.getItemAt(0);
                        CharSequence clipDataItemText = clipDataItem.getText();
                        JsbBridgeCallback.getInstance().sendToScript(event, clipDataItemText.toString());
                    }
                } else if (TextUtils.equals(event, "getOpenInstall")) {
                    if (TextUtils.isEmpty(openInstallKey)) return;
                    OpenInstall.getInstall(new AppInstallAdapter() {
                        @Override
                        public void onInstall(@NonNull AppData appData) {
                            String data = appData.getData();
                            sendToScript(event, data);
                        }
                    });
                } else if (TextUtils.equals(event, "getOaid")) {
                    String oaid = getOaid(context);
                    if (TextUtils.isEmpty(oaid)) oaid = "";
                    sendToScript(event, oaid);
                } else if (TextUtils.equals(event, "getAndroidID")) {
                    String androidID = "";
                    try {
                        androidID = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (androidID == null) androidID = "";
                    sendToScript(event, androidID);
                } else if (TextUtils.equals(event, "getBrand")) {
                    String brand = Build.BRAND;
                    if (brand == null) brand = "";
                    sendToScript(event, brand);
                } else if (TextUtils.equals(event, "getManufacturer")) {
                    String manufacturer = Build.MANUFACTURER;
                    if (manufacturer == null) manufacturer = "";
                    sendToScript(event, manufacturer);
                } else if (TextUtils.equals(event, "getModel")) {
                    String model = Build.MODEL;
                    if (model == null) model = "";
                    sendToScript(event, model);
                } else if (TextUtils.equals(event, "isEmulator")) {
                    boolean isEmulator = EasyProtectorLib.checkIsRunningInEmulator(context, null);
                    sendToScript(event, String.valueOf(isEmulator));
                } else if (TextUtils.equals(event, "isAccessibilityServiceEnabled")) {
                    boolean isAccessibilityServiceEnabled = false;
                    AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
                    if (accessibilityManager.isEnabled()) {
                        List<AccessibilityServiceInfo> enabledServices =
                                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
                        isAccessibilityServiceEnabled = enabledServices.stream()
                                .anyMatch(match -> TextUtils.equals(context.getPackageName(), match.getId()));
                    }
                    sendToScript(event, String.valueOf(isAccessibilityServiceEnabled));
                } else if (TextUtils.equals(event, "isDeveloperModeEnabled")) {
                    boolean isUsbEnabled = false;
                    try {
                        @SuppressLint("PrivateApi")
                        Class<?> clazz = Class.forName("android.os.SystemProperties");
                        Method get = clazz.getMethod("get", String.class, String.class);
                        String value = (String) (get.invoke(clazz, "persist.sys.usb.config", ""));
                        isUsbEnabled = TextUtils.equals("adb", value);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    boolean isDevelopmentEnabled =
                            Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
                    boolean isAdbEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
                    boolean isDeveloperModeEnabled = isUsbEnabled || isDevelopmentEnabled || isAdbEnabled;
                    sendToScript(event, String.valueOf(isDeveloperModeEnabled));
                } else if (TextUtils.equals(event, "getPhone")) {
                    String phone = PhoneUtil.getPhone(context);
                    sendToScript(event, phone);
                } else if (TextUtils.equals(event, "launchApp")) {
                    String scheme = data.getString("scheme");
                    if (!TextUtils.isEmpty(scheme)) {
                        try {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(scheme));
                            context.startActivity(intent);
                        } catch (Exception e) {
                            ToastKt.make(context, "打开失败", ToastKt.LENGTH_LONG).show();
                        }
                    }
                } else if (TextUtils.equals(event, "getPackageName")) {
                    String packageName = context.getPackageName();
                    sendToScript(event, packageName);
                } else if (TextUtils.equals(event, "iqyQiLin")) {
                    String subEvent = data.getString("event");
                    if (TextUtils.equals("init", subEvent)) {
                        String appId = data.getString("appId");
                        String channelId = data.getString("channelId");
                        String oaid = data.getString("oaid");
                        QiLin.getInstance().init(context, appId, channelId, oaid);
                    } else if (TextUtils.equals("trans", subEvent)) {
                        String type = data.getString("type");
                        String param = data.getString("param");
                        if (TextUtils.isEmpty(param)) {
                            QiLin.getInstance().uploadTrans(type, null);
                        } else {
                            try {
                                org.json.JSONObject params = new org.json.JSONObject(param);
                                QiLin.getInstance().uploadTrans(type, params);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                QiLin.getInstance().uploadTrans(type, null);
                            }
                        }
                    }
                } else if (TextUtils.equals(event, "getNetworkType")) {
                    String networkType = NetworkUtil.getNetworkType(context);
                    sendToScript(event, String.format("{\"networkType\":%s}", networkType));
                } else if (TextUtils.equals(event, "asrRealtime")) {
                    AsrRealtimeUtil.getInstance().handler(context, event, data);
                }
            });
        };
    }

    public void onResume() {
        QiLin.getInstance().onResume();
    }

    public void onDestroy() {
        QiLin.getInstance().onDestroy();
        PayUtil.unregister(mContext);
    }
}
