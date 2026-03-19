package com.cocos.game.sigmobAdManager;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.sigmob.windad.Splash.WindSplashAD;
import com.sigmob.windad.Splash.WindSplashADListener;
import com.sigmob.windad.Splash.WindSplashAdRequest;
import com.sigmob.windad.WindAdError;

import java.util.HashMap;
import java.util.Map;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private WindSplashAD ad;
    private final Map<String, Object> options = new HashMap<>();

    private AdMainCallBack m_adMainCallBack;

    // 获取&创建单例类
    public static AdSplash getInstance() {
        if (instance == null) {
            instance = new AdSplash();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {

        // 加载开屏广告
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "开屏广告", id, "sigmob");
            return m_adMainCallBack;
        }

        // placementId 必填，user_id、options可不填，
        WindSplashAdRequest request = new WindSplashAdRequest(id, null, options);
        request.setDisableAutoHideAd(true);
        request.setFetchDelay(5);
        ad = new WindSplashAD(request, new WindSplashADListener() {
            @Override
            public void onSplashAdLoadSuccess(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功", "开屏广告", id, "sigmob");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onSplashAdLoadFail(WindAdError windAdError, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载错误, code:%s, msg:%s", "开屏广告", id, "sigmob", windAdError.getErrorCode(), windAdError.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, windAdError.getErrorCode(), windAdError.getMessage());
                }
            }

            @Override
            public void onSplashAdShow(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示成功", "开屏广告", id, "sigmob");
                JSONObject obj = new JSONObject();
                String ecpm = ad.getEcpm();
                obj.put("adv_no", id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onSplashAdShowError(WindAdError windAdError, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示失败, code:%s, msg:%s", "开屏广告", id, "sigmob", windAdError.getErrorCode(), windAdError.getMessage());
            }

            @Override
            public void onSplashAdClick(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "开屏广告", id, "sigmob");
            }

            @Override
            public void onSplashAdClose(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "开屏广告", id, "sigmob");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                m_mainInstance.getMainView().removeAllViews();
                ad.destroy();
            }

            @Override
            public void onSplashAdSkip(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 被跳过", "开屏广告", id, "sigmob");
                m_mainInstance.getMainView().removeAllViews();
                ad.destroy();
            }
        });

        ad.loadAd();

        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd(String id) {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || ad == null || !isReady", "开屏广告", id, "sigmob");
            return;
        }
        ad.show(container);
    }
}
