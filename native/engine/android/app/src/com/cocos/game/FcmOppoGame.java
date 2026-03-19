package com.cocos.game;

import android.app.Activity;
import android.content.Context;

import com.nearme.game.sdk.GameCenterSDK;
import com.nearme.game.sdk.callback.ApiCallback;
import com.nearme.game.sdk.common.util.AppUtil;

public class FcmOppoGame {
    private boolean inited = false;
    private boolean logining = false;
    private static final long delay = 1000;

    private FcmOppoGame() {
    }

    private static final class InstanceHolder {
        private static final FcmOppoGame mInstance = new FcmOppoGame();
    }

    public static FcmOppoGame getInstance() {
        return InstanceHolder.mInstance;
    }

    public boolean isInited() {
        return inited;
    }

    public void initOppoGame(Activity activity, String appSecret, boolean login) {
        if (!inited) {
            GameCenterSDK.init(appSecret, activity);
            inited = true;
        }
        if (!login) return;
        if (logining) return;
        logining = true;
        LoadingDialog.getInstance().show();
        activity.getWindow().getDecorView().postDelayed(() -> login(activity), delay);
    }

    public void login(Context context) {
        GameCenterSDK.getInstance().doLogin(context, new ApiCallback() {
            @Override
            public void onSuccess(String resultMsg) {
                logining = false;
                LoadingDialog.getInstance().hide();
                // {"token":"","ssoid":""}
                Logger.e("", String.format("OppoGame result:%s", resultMsg));
                JsbBridgeCallback.getInstance().sendToScript("initFcm", resultMsg);
            }

            @Override
            public void onFailure(String resultMsg, int resultCode) {
                logining = false;
                LoadingDialog.getInstance().hide();
                Logger.e("", String.format("OppoGame code:%s, msg:%s", resultCode, resultMsg));
                JsbBridgeCallback.getInstance().sendToScript("initFcm", "");
            }
        });
    }

    public void exit(Activity activity) {
        GameCenterSDK.getInstance().onExit(activity, () -> AppUtil.exitGameProcess(activity));
    }

}
