package com.cocos.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.volcengine.mobsecBiz.metasec.listener.ITokenObserver;
import com.volcengine.mobsecBiz.metasec.ml.MSConfig;
import com.volcengine.mobsecBiz.metasec.ml.MSManager;
import com.volcengine.mobsecBiz.metasec.ml.MSManagerUtils;

public class VolcengineUtil {
    private VolcengineUtil() {
    }

    private static final class InstanceHolder {
        private static final VolcengineUtil mInstance = new VolcengineUtil();
    }

    public static VolcengineUtil getInstance() {
        return InstanceHolder.mInstance;
    }

    public void initVolcengine(Context context, String appId, String license, String channel, String scene) {
        SharedPreferences game = context.getSharedPreferences("game", Context.MODE_PRIVATE);
        String token = game.getString("volcengine_token", "");

        if (TextUtils.isEmpty(token) && !TextUtils.isEmpty(appId)) {
            MSManager mgr = MSManagerUtils.get(appId);
            if (mgr != null) {
                token = mgr.getToken();
                game.edit().putString("volcengine_token", token).apply();
            }
        }

        if (!TextUtils.isEmpty(token)) {
            JsbBridgeCallback.getInstance().sendToScript("initVolcengine", token);
            if (!TextUtils.isEmpty(appId)) {
                if (TextUtils.isEmpty(scene)) scene = "game";
                // 主动上报
                // 对于某些关键场景，比如登录、支付等场景，可以主动调用上报接口，确认数据上报的及时性；
                // 如不配置主动上报，设备数据将只在安全SDK初始化完成后上报一次。
                MSManager mgr = MSManagerUtils.get(appId);
                if (mgr != null) mgr.report(scene);
            }
            return;
        }

        if (TextUtils.isEmpty(appId) || TextUtils.isEmpty(license)) {
            JsbBridgeCallback.getInstance().sendToScript("initVolcengine", "");
            return;
        }
        if (TextUtils.isEmpty(channel)) channel = "game";

        // 在火山引擎控制台上配置应用并获取appId(应用ID)
        // 鉴权，在火山引擎控制台上完成配置
        // 正常采集模式：MSConfig.COLLECT_MODE_DEFAULT
        // 基础采集模式：MSConfig.COLLECT_MODE_ML_MINIMIZE
        MSConfig.Builder builder = new MSConfig.Builder(appId, license, MSConfig.COLLECT_MODE_DEFAULT);
        MSConfig config = builder
                .setChannel(channel)
                // 注意：此开关设置，在安全SDK初始化过程中，如果APP切到后台，会中断所有数据的采集，APP再次切回前台时恢复采集，默认为不开启
                .addAdvanceInfo("controllable", MSConfig.CONTROL_MODE_ON)
                .addDataObserver(new ITokenObserver() {
                    @Override
                    public void onTokenLoaded(String token) {
                        token = TextUtils.isEmpty(token) ? "" : token;
                        game.edit().putString("volcengine_token", token).apply();
                        // 此为回调方法，在SDK获取到设备token之后主动回调，可选，非必须使用回调
                        JsbBridgeCallback.getInstance().sendToScript("initVolcengine", token);
                    }
                })
                .build();

        // 初始化安全SDK
        // 注意:此版本安全SDK初始化不会在APP本地采集任何数据
        MSManagerUtils.init(context, config);

        // 初始化token，调用此接口会开始采集并上报设备数据以及获取设备token，强烈建议在初始化之后立即调用，避免缺失APP启动时的风险识别能力
        MSManagerUtils.initToken(appId);
    }
}
