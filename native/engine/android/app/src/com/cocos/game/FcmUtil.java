package com.cocos.game;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.m3839.sdk.single.UnionFcmParam;
import com.m3839.sdk.single.UnionFcmSDK;
import com.m3839.sdk.single.UnionFcmUser;
import com.m3839.sdk.single.UnionV2FcmListener;
import com.taptap.sdk.compliance.TapTapCompliance;
import com.taptap.sdk.compliance.TapTapComplianceCallback;
import com.taptap.sdk.compliance.constants.ComplianceMessage;
import com.taptap.sdk.compliance.option.TapTapComplianceOptions;
import com.taptap.sdk.core.TapTapRegion;
import com.taptap.sdk.core.TapTapSdk;
import com.taptap.sdk.core.TapTapSdkOptions;
import com.taptap.sdk.kit.internal.callback.TapTapCallback;
import com.taptap.sdk.kit.internal.exception.TapTapException;
import com.taptap.sdk.login.Scopes;
import com.taptap.sdk.login.TapTapAccount;
import com.taptap.sdk.login.TapTapLogin;

import java.util.Map;

import cn.m4399.operate.AdGame;
import cn.m4399.operate.OpeInitedListener;
import cn.m4399.operate.OperateConfig;

public class FcmUtil {
    private FcmUtil() {
    }

    private static final class InstanceHolder {
        private static final FcmUtil mInstance = new FcmUtil();
    }

    public static FcmUtil getInstance() {
        return InstanceHolder.mInstance;
    }

    public void initOppoGame(Activity activity, String appSecret, boolean login) {
        FcmOppoGame.getInstance().initOppoGame(activity, appSecret, login);
    }

    public boolean isInitedOppoGame() {
        return FcmOppoGame.getInstance().isInited();
    }

    public void exitOppoGame(Activity activity) {
        FcmOppoGame.getInstance().exit(activity);
    }

    public void initYSdk(Activity activity, boolean login) {
        FcmYSDK.getInstance().initYSDK(activity, login);
    }

    public void initTaptap(Activity activity, String gameId, String gameToken) {
        if (activity == null || TextUtils.isEmpty(gameId) || TextUtils.isEmpty(gameToken)) return;

        TapTapSdkOptions sdk = new TapTapSdkOptions(gameId, gameToken, TapTapRegion.CN);
        sdk.setEnableLog(true);

        TapTapComplianceOptions compliance = new TapTapComplianceOptions(false, true);
        TapTapSdk.init(activity, sdk, compliance);

        TapTapCompliance.registerComplianceCallback(new TapTapComplianceCallback() {
            @Override
            public void onComplianceResult(int code, @Nullable Map<String, ?> extra) {
                String msg = "";
                // https://developer.taptap.cn/docs/sdk/anti-addiction/guide/#回调设置
                switch (code) {
                    case ComplianceMessage.LOGIN_SUCCESS: // 500
                        msg = "玩家未受到限制，正常进入游戏";
                        break;
                    case ComplianceMessage.EXITED: // 1000
                        msg = "退出防沉迷认证及检查，当开发者调用 Exit 接口时或用户认证信息无效时触发，游戏应返回到登录页";
                        break;
                    case ComplianceMessage.SWITCH_ACCOUNT: // 1001
                        msg = "用户点击切换账号，游戏应返回到登录页";
                        break;
                    case ComplianceMessage.PERIOD_RESTRICT: // 1030
                        msg = "用户当前时间无法进行游戏，此时用户只能退出游戏或切换账号";
                        break;
                    case ComplianceMessage.DURATION_LIMIT: // 1050
                        msg = "用户无可玩时长，此时用户只能退出游戏或切换账号";
                        break;
                    case ComplianceMessage.AGE_LIMIT: // 1100
                        msg = "当前用户因触发应用设置的年龄限制无法进入游戏";
                        break;
                    case ComplianceMessage.INVALID_CLIENT_OR_NETWORK_ERROR: // 1200
                        msg = "数据请求失败，游戏需检查当前设置的应用信息是否正确及判断当前网络连接是否正常";
                        break;
                    case ComplianceMessage.REAL_NAME_STOP: // 9002
                        msg = "实名过程中点击了关闭实名窗，游戏可重新开始防沉迷认证";
                        break;
                    default:
                        break;
                }
                Logger.e("", String.format("TapTapSdk code:%s, msg:%s", code, msg));
                JsbBridgeCallback.getInstance().sendToScript("initFcm", String.format("{\"code\":%s,\"msg\":\"%s\"}", code, msg));
            }
        });

        String[] scopes = new String[]{Scopes.SCOPE_BASIC_INFO};
        TapTapLogin.loginWithScopes(activity, scopes, new TapTapCallback<>() {
            @Override
            public void onSuccess(TapTapAccount account) {
                if (account != null) {
                    String openId = account.getOpenId();
                    String unionId = account.getUnionId();
                    Logger.e("", String.format("TapTapAccount account openId:%s, unionId:%s", openId, unionId));
                    JsbBridgeCallback.getInstance().sendToScript("initFcm", String.format("{\"openid\":%s,\"unionid\":\"%s\"}", openId, unionId));
                    TapTapCompliance.startup(activity, unionId);
                } else {
                    Logger.e("", String.format("TapTapAccount account %s", "is null"));
                }
            }

            @Override
            public void onCancel() {
                Logger.e("", String.format("TapTapLogin %s", "onCancel"));
            }

            @Override
            public void onFail(@NonNull TapTapException e) {
                Logger.e("", String.format("TapTapLogin %s", "onFail"));
                e.printStackTrace();
            }
        });
    }

    public void initHykb(Activity activity, String gameId) {
        if (activity == null || TextUtils.isEmpty(gameId)) return;

        UnionFcmParam param = new UnionFcmParam.Builder()
                .setGameId(gameId)
                .setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .build();
        UnionV2FcmListener listener = new UnionV2FcmListener() {
            @Override
            public void onSucceed(UnionFcmUser user) {
                if (user != null) {
                    String userId = user.getUserId();
                    String nick = user.getNick();
                    Logger.d("", String.format("UnionFcmSDK.init: onSucceed --> userId: %s, nick: %s", userId, nick));
                    JsbBridgeCallback.getInstance().sendToScript("initFcm", String.format("{\"userid\":%s,\"nick\":\"%s\"}", userId, nick));
                } else {
                    Logger.d("", "UnionFcmSDK.init: onSucceed --> user is null");
                    JsbBridgeCallback.getInstance().sendToScript("initFcm", "");
                }
            }

            @Override
            public void onFailed(int code, String msg) {
                Logger.d("", String.format("UnionFcmSDK.init: onFailed --> code: %s, msg: %s", code, msg));
                JsbBridgeCallback.getInstance().sendToScript("initFcm", "");
            }
        };
        UnionFcmSDK.init(activity, param, listener);
    }

    public void initM4399(Activity activity, String gameId) {
        if (activity == null || TextUtils.isEmpty(gameId)) return;

        OperateConfig config = new OperateConfig.Builder(activity)
                .setDebugEnabled(true)
                .setGameKey(gameId)
                .setOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                .setSupportExcess(false)
                .compatNotch(true)
                .build();
        OpeInitedListener listener = () -> {
            JsbBridgeCallback.getInstance().sendToScript("initFcm", "1");
            // 验证通过，可以进入游戏
            Logger.d("", "AdGame.init: onInitFinished");
        };
        AdGame.init(activity, config, listener);
    }
}
