package com.cocos.game;

import android.app.Activity;

import com.tencent.ysdk.api.YSDKApi;
import com.tencent.ysdk.framework.common.eFlag;
import com.tencent.ysdk.framework.common.ePlatform;
import com.tencent.ysdk.module.antiaddiction.listener.AntiAddictListener;
import com.tencent.ysdk.module.antiaddiction.model.AntiAddictRet;
import com.tencent.ysdk.module.user.UserListener;
import com.tencent.ysdk.module.user.UserLoginRet;
import com.tencent.ysdk.module.user.UserRelationRet;
import com.tencent.ysdk.module.user.WakeupRet;

import java.util.function.Consumer;

public class FcmYSDK {
    private boolean inited = false;
    private final Consumer<String> listener = value -> {
        JsbBridgeCallback.getInstance().sendToScript("initFcm", value);
    };
    private boolean logining = false;
    private static final long delay = 1000;
    private final int retryMax = 2;
    private int retry = retryMax;

    private FcmYSDK() {
    }

    private static final class InstanceHolder {
        private static final FcmYSDK mInstance = new FcmYSDK();
    }

    public static FcmYSDK getInstance() {
        return InstanceHolder.mInstance;
    }

    public void initYSDK(Activity activity, boolean login) {
        if (!inited) {
            // 1. 初始化YSDK
            YSDKApi.init(false);
            listenerYSDK(listener);

            inited = true;
        }
        if (!login) return;
        if (logining) return;
        logining = true;
        LoadingDialog.getInstance().show();
        activity.getWindow().getDecorView().postDelayed(this::loginYSDK, delay);
    }

    private void listenerYSDK(Consumer<String> listener) {
        // 2. 接入单机账号模式
        // 2.1. 设置全局监听
        YSDKApi.setUserListener(new UserListener() {
            @Override
            public void OnLoginNotify(UserLoginRet userLoginRet) {
                logining = false;
                LoadingDialog.getInstance().hide();
                int code = userLoginRet.flag;
                String msg = "";
                // https://wikinew.open.qq.com/index.html#/iwiki/853061422
                switch (code) {
                    case eFlag.Succ:
                        msg = "玩家未受到限制，正常进入游戏";
                        if (userLoginRet.getLoginType() != UserLoginRet.LOGIN_TYPE_TIMER) {
                            // 定时登录，不需要设置防沉迷统计开始
                            YSDKApi.setAntiAddictGameStart();
                        }
                        break;
                    case eFlag.QQ_UserCancel:
                        msg = "手Q返回用户取消 引导用户重新授权或者分享";
                        YSDKApi.logout();
                        break;
                    case eFlag.QQ_LoginFail:
                        msg = "手Q返回登录失败 引导用户重新授权";
                        YSDKApi.logout();
                        break;
                    case eFlag.QQ_NetworkErr:
                        msg = "手Q提示登录时网路异常 引导用户检查网络后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.QQ_NotInstall:
                        msg = "用户手机没有安装手Q 引导用户安装手Q后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.QQ_NotSupportApi:
                        msg = "用户手机手Q版本太低 引导用户升级手Q后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.WX_NotInstall:
                        msg = "用户手机没有安装微信 引导用户安装微信后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.WX_NotSupportApi:
                        msg = "用户手机微信版本太低 引导用户升级微信后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.WX_UserCancel:
                        msg = "用户取消授权 引导用户重新授权或者分享";
                        YSDKApi.logout();
                        break;
                    case eFlag.WX_UserDeny:
                        msg = "用户拒绝授权 引导用户重新授权";
                        YSDKApi.logout();
                        break;
                    case eFlag.WX_LoginFail:
                        msg = "微信返回登录失败 引导用户重新授权";
                        YSDKApi.logout();
                        break;
                    case 10005:
                        msg = "微信参数没有登录功能，需要到微信开放平台开发者资质做认证，请认证后重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.GUEST_LoginFail:
                        msg = "单机账号登录失败 引导用户再次尝试";
                        YSDKApi.logout();
                        break;
                    case eFlag.Login_TokenInvalid:
                        msg = "本地票据不可用 引导用户重新授权";
                        YSDKApi.logout();
                        if (retry > 0) {
                            retry -= 1;
                            loginYSDK();
                        }
                        break;
                    case eFlag.Login_NotRegisterRealName:
                        msg = "用户账号没有实名认证 引导用户重新授权并认证权";
                        YSDKApi.logout();
                        break;
                    case eFlag.Login_CheckingToken:
                        msg = "YSDK 自动登录中 无需关注,可以按照成功处理";
                        YSDKApi.logout();
                        break;
                    case eFlag.Login_NeedRegisterRealName:
                        msg = "用户需要实名认证，游戏方取消超时验证逻辑，无需通知用户";
                        YSDKApi.logout();
                        break;
                    case eFlag.Login_Free_Login_Auth_Failed:
                        msg = "YSDK 免登录信息校验失败";
                        YSDKApi.logout();
                        break;
                    case eFlag.Login_User_Logout:
                        msg = "用户退出登录";
                        YSDKApi.logout();
                        break;
                    case eFlag.Relation_RelationNoPerson:
                        msg = "没有查询到信息 再次调用接口重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.Wakeup_NeedUserLogin:
                        msg = "应用被拉起时无登录态 显示登录按钮,引导用户授权";
                        YSDKApi.logout();
                        break;
                    case eFlag.Wakeup_YSDKLogining:
                        msg = "YSDK自动登录中游戏无需关注（登录结束结果会通过LoginNotify回调）";
                        YSDKApi.logout();
                        break;
                    case eFlag.Wakeup_NeedUserSelectAccount:
                        msg = "拉起游戏时存在异常账号 游戏需要弹框让用户选择登入游戏的账号";
                        YSDKApi.logout();
                        break;
                    case eFlag.Pay_User_Cancle:
                        msg = "用户取消支付 引导用户重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.Pay_Param_Error:
                        msg = "支付参数错误 检查参数以后,引导用户重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.Sandbox_User_No_Login:
                        msg = "没有获取到应用宝登录态";
                        YSDKApi.logout();
                        break;
                    case eFlag.Sandbox_User_No_Support:
                        msg = "暂不支持的登录类型";
                        YSDKApi.logout();
                        break;
                    case eFlag.Certification_No_Found_Error:
                        msg = "没有查询到信息 再次调用接口重试";
                        YSDKApi.logout();
                        break;
                    case eFlag.METHOD_DEPRECATED:
                        msg = "方法已经废弃";
                        YSDKApi.logout();
                        break;
                    case eFlag.Cg_Login_Platform_Not_Support:
                        msg = "云游不支持该登录平台";
                        YSDKApi.logout();
                        break;
                    case eFlag.Cg_Login_Platform_Wx_Code_Empty:
                        msg = "云游微信的授权码为空";
                        YSDKApi.logout();
                        break;
                    case eFlag.Cg_Platform_Not_Found:
                        msg = "云游该登录平台没找到";
                        YSDKApi.logout();
                        break;
                    case eFlag.Cg_Yyb_Return_Auth_Login_Result:
                        msg = "云游戏应用宝返回授权登录失败或者取消";
                        YSDKApi.logout();
                        break;
                    case 100044:
                        msg = "游戏当前安装包签名与平台上传的安装包签名不一致，请检查打包用的keystore";
                        YSDKApi.logout();
                        break;
                    case 110406:
                        msg = "未正式上线的游戏，需要使用调试者账号测试，非调试者账号登录则报错110406。添加的调试者账号必须与开发者账号是好友";
                        YSDKApi.logout();
                        break;
                    case 110401:
                    case 110407:
                        msg = "腾讯开放平台必须和QQ互联平台进行关联，关联时需与应用宝后台该游戏保持手QAPPID、手QAPPKEY、ICON、签名及包名一致，且审核已通过（QQ互联请使用腾讯开放平台开发者账号登录 https://connect.qq.com）";
                        YSDKApi.logout();
                        break;
                    default:
                        break;
                }
                Logger.e("", String.format("YSDKApi code:%s, msg:%s", code, msg));
                if (listener != null)
                    listener.accept(String.format("{\"code\":%s,\"msg\":\"%s\"}", code, msg));
            }

            @Override
            public void OnWakeupNotify(WakeupRet wakeupRet) {
                logining = false;
                LoadingDialog.getInstance().hide();
                Logger.e("", "YSDKApi OnWakeupNotify");
            }

            @Override
            public void OnRelationNotify(UserRelationRet userRelationRet) {
                logining = false;
                LoadingDialog.getInstance().hide();
                Logger.e("", "YSDKApi OnRelationNotify");
            }
        });

        YSDKApi.setAntiAddictListener(new AntiAddictListener() {
            @Override
            public void onLoginLimitNotify(AntiAddictRet antiAddictRet) {
                onLimitNotify(antiAddictRet, listener);
            }

            @Override
            public void onTimeLimitNotify(AntiAddictRet antiAddictRet) {
                onLimitNotify(antiAddictRet, listener);
            }
        });
    }

    private void loginYSDK() {
        retry = retryMax;
        // 2.2. 发起登录 - 单机账号模式
        YSDKApi.login(ePlatform.Guest);
    }

    private void onLimitNotify(AntiAddictRet antiAddictRet, Consumer<String> listener) {
        switch (antiAddictRet.ruleFamily) {
            case AntiAddictRet.RULE_WORK_TIP:
            case AntiAddictRet.RULE_WORK_NO_PLAY:
            case AntiAddictRet.RULE_HOLIDAY_TIP:
            case AntiAddictRet.RULE_HOLIDAY_NO_PLAY:
            case AntiAddictRet.RULE_NIGHT_NO_PLAY:
            case AntiAddictRet.RULE_GUEST:
            default:
                executeInstruction(antiAddictRet, listener);
                break;
        }
    }

    private void executeInstruction(AntiAddictRet ret, Consumer<String> listener) {
        switch (ret.type) {
            // 防沉迷指令-弹窗提示
            case AntiAddictRet.TYPE_TIPS:
                if (listener != null)
                    listener.accept(String.format("{\"code\":%s,\"msg\":\"%s\"}",
                            AntiAddictRet.TYPE_TIPS, String.format("%s: %s", ret.title, ret.content)));
                break;
            // 防沉迷指令-强制下线
            case AntiAddictRet.TYPE_LOGOUT:
                if (listener != null)
                    listener.accept(String.format("{\"code\":%s,\"msg\":\"%s\"}",
                            AntiAddictRet.TYPE_LOGOUT, String.format("%s: %s", ret.title, ret.content)));
                break;
            default:
                // do nothing
                break;
        }

        // 已执行指令
        YSDKApi.reportAntiAddictExecute(ret, System.currentTimeMillis());
    }
}
