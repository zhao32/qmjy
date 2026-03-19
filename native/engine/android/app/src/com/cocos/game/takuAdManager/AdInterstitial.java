package com.cocos.game.takuAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATShowConfig;
import com.anythink.core.api.AdError;
import com.anythink.interstitial.api.ATInterstitial;
import com.anythink.interstitial.api.ATInterstitialListener;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, ATInterstitial> maps = new HashMap<>();

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "taku");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "taku");
            return m_adMainCallBack;
        }

        ATInterstitial ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "taku");
            loadFullScreenVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "插屏广告", id, "taku");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadFullScreenVideoAd(String id) {
        maps.remove(id);
        ATInterstitial ad = new ATInterstitial(m_mainInstance.getGameCtx(), id);
        ad.setAdListener(new ATInterstitialListener() {
            @Override
            public void onInterstitialAdLoaded() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "taku", preload ? "预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onInterstitialAdLoadFail(AdError adError) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "插屏广告", id, "taku", preload ? "预加载" : "", adError.getCode(), adError.getDesc());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, Integer.parseInt(adError.getCode()), adError.getDesc());
                }
            }

            @Override
            public void onInterstitialAdClicked(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "插屏广告", id, "taku");
            }

            @Override
            public void onInterstitialAdShow(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示成功", "插屏广告", id, "taku");
                if (atAdInfo != null) {
                    JSONObject obj = new JSONObject();
                    String requestId = atAdInfo.getRequestId();
                    double ecpm = atAdInfo.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "interstitialAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onInterstitialAdClose(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "插屏广告", id, "taku");
                preload = true;
                loadFullScreenVideoAd(id);
            }

            @Override
            public void onInterstitialAdVideoStart(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放开始", "插屏广告", id, "taku");
            }

            @Override
            public void onInterstitialAdVideoEnd(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "插屏广告", id, "taku");
            }

            @Override
            public void onInterstitialAdVideoError(AdError adError) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放失败 code:%s msg:%s", "插屏广告", id, "taku", adError.getCode(), adError.getDesc());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, adError, Integer.parseInt(adError.getCode()), adError.getDesc());
                }
            }
        });
        ad.load();
    }

    private ATShowConfig getATShowConfig() {
        ATShowConfig.Builder builder = new ATShowConfig.Builder();
        builder.scenarioId("reward_show");
        builder.showCustomExt("reward_custom_ext");
        return builder.build();
    }

    // 展示插屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "taku");
            return;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        ATInterstitial ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isAdReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !ad.isAdReady", "插屏广告", id, "taku");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "插屏广告", id, "taku");
        ad.show(activity, getATShowConfig());
    }
}
