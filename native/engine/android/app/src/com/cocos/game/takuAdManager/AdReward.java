package com.cocos.game.takuAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATShowConfig;
import com.anythink.core.api.AdError;
import com.anythink.rewardvideo.api.ATRewardVideoAd;
import com.anythink.rewardvideo.api.ATRewardVideoListener;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, ATRewardVideoAd> maps = new HashMap<>();

    public static AdReward getInstance() {
        if (instance == null) {
            instance = new AdReward();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    // 加载激励视频
    public AdMainCallBack LoadAd(String id) {
        reward = false;
        preload = false;

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "taku");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "taku");
            return m_adMainCallBack;
        }

        ATRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "taku");
            loadRewardVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "激励广告", id, "taku");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        ATRewardVideoAd ad = new ATRewardVideoAd(m_mainInstance.getGameCtx(), id);
        ad.setAdListener(new ATRewardVideoListener() {
            @Override
            public void onRewardedVideoAdLoaded() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "taku", preload ? "预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onRewardedVideoAdFailed(AdError adError) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "taku", preload ? "预加载" : "", adError.getCode(), adError.getDesc());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, adError, Integer.parseInt(adError.getCode()), adError.getDesc());
                }
            }

            @Override
            public void onRewardedVideoAdPlayStart(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "taku");
                if (atAdInfo != null) {
                    JSONObject obj = new JSONObject();
                    String requestId = atAdInfo.getRequestId();
                    double ecpm = atAdInfo.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "rewardedVideoAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onRewardedVideoAdPlayEnd(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "taku");
            }

            @Override
            public void onRewardedVideoAdPlayFailed(AdError adError, ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放失败 code:%s msg:%s", "激励广告", id, "taku", adError.getCode(), adError.getDesc());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, adError, Integer.parseInt(adError.getCode()), adError.getDesc());
                }
            }

            @Override
            public void onRewardedVideoAdClosed(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放关闭", "激励广告", id, "taku");
                onRewardVideo(ad, atAdInfo, id);
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onRewardedVideoAdPlayClicked(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "激励广告", id, "taku");
            }

            @Override
            public void onReward(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 获得奖励", "激励广告", id, "taku");
                if (!reward) reward = true;
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

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "taku");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        ATRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isAdReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !ad.isAdReady", "激励广告", id, "taku");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "taku");
        // 展示激励视频
        ad.show(activity, getATShowConfig());
    }

    private void onRewardVideo(ATRewardVideoAd ad, ATAdInfo atAdInfo, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "taku");
        obj.put("adv_id", id);
        if (reward && ad != null && atAdInfo != null) {
            String requestId = atAdInfo.getRequestId();
            double ecpm = atAdInfo.getEcpm();
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
