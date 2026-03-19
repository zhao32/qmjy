package com.cocos.game.sigmobMillAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.interstitial.WMInterstitialAd;
import com.windmill.sdk.interstitial.WMInterstitialAdListener;
import com.windmill.sdk.interstitial.WMInterstitialAdRequest;
import com.windmill.sdk.models.AdInfo;

import java.util.HashMap;
import java.util.Map;

public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private WMInterstitialAd ad;
    private final Map<String, Object> options = new HashMap<>();
    private AdMainCallBack m_adMainCallBack;

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("[%s] act == null || id == null", "插屏广告");
            return m_adMainCallBack;
        }

        // placementId 必填，user_id、options可不填，
        WMInterstitialAdRequest request = new WMInterstitialAdRequest(id, null, options);
        ad = new WMInterstitialAd(activity, request);
        ad.setInterstitialAdListener(new WMInterstitialAdListener() {
            @Override
            public void onInterstitialAdLoadSuccess(String placementId) {
                m_mainInstance.DebugPrintE("[%s] 加载成功", "插屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, null);
                }
            }

            @Override
            public void onInterstitialAdPlayStart(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 开始展示", "插屏广告");
                JSONObject obj = new JSONObject();
                String requestId = adInfo.getLoadId();
                String ecpm = adInfo.geteCPM();
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onInterstitialAdPlayEnd(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 结束展示", "插屏广告");
            }

            @Override
            public void onInterstitialAdClicked(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 被用户点击", "插屏广告");
            }

            @Override
            public void onInterstitialAdClosed(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "插屏广告");
            }

            @Override
            public void onInterstitialAdLoadError(WindMillError error, String placementId) {
                // 广告加载或展示过程中出错
                m_mainInstance.DebugPrintE("[%s] 加载错误, code:%s, msg:%s", "插屏广告", error.getErrorCode(), error.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, error.getErrorCode(), error.getMessage());
                }
            }

            @Override
            public void onInterstitialAdPlayError(WindMillError error, String placementId) {
                m_mainInstance.DebugPrintE("[%s] 展示失败", "插屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, error.getErrorCode(), error.getMessage());
                }
            }
        });

        ad.loadAd();

        return m_adMainCallBack;
    }

    // 展示插全屏广告
    public void ShowAd() {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("[%s] act == null || ad == null || !isReady", "插屏广告");
            return;
        }
        HashMap<String, String> showOption = new HashMap<>();
        ad.show(activity, showOption);
    }

}
