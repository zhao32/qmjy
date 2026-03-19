package com.cocos.game.csjAdManager;

import android.app.Activity;
import android.os.Bundle;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTRewardVideoAd;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationAdEcpmInfo;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationBaseManager;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationRewardManager;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean rewarded = false;
    private boolean preload = false;
    private TTAdNative adNativeLoader;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, AdSlot> slots = new HashMap<>();
    private final Map<String, TTRewardVideoAd> maps = new HashMap<>();

    public static AdReward getInstance() {
        if (instance == null) {
            instance = new AdReward();
            instance.m_mainInstance = AdMain.getInstance();
            instance.adNativeLoader = TTAdSdk.getAdManager().createAdNative(instance.m_mainInstance.getGameCtx());
        }
        return instance;
    }

    // 加载激励视频
    public AdMainCallBack LoadAd(String id) {
        reward = false;
        rewarded = false;
        preload = false;

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "csj");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "csj");
            return m_adMainCallBack;
        }

        TTRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "csj");
            loadRewardVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "激励广告", id, "csj");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        rewarded = false;
        maps.remove(id);
        AdSlot adSlot = null;
        if (slots.containsKey(id)) {
            adSlot = slots.get(id);
        }
        if (adSlot == null) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(id)  // 广告位ID
                    .setAdLoadType(TTAdLoadType.LOAD)
                    .setOrientation(TTAdConstant.VERTICAL)  // 激励视频方向 横竖屏设置
                    .build();
            slots.put(id, adSlot);
        }
        // 这里为激励视频的简单功能，如需使用复杂功能，如gromore的服务端奖励验证，请参考demo中的AdUtils.kt类中激励部分
        if (adNativeLoader == null)
            adNativeLoader = TTAdSdk.getAdManager().createAdNative(m_mainInstance.getGameCtx());
        adNativeLoader.loadRewardVideoAd(adSlot, new TTAdNative.RewardVideoAdListener() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "csj", preload ? "预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onRewardVideoAdLoad(TTRewardVideoAd ttRewardVideoAd) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "csj", preload ? "预加载" : "");
            }

            @Override
            public void onRewardVideoCached() {
                // 广告缓存成功 此api已经废弃，请使用onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd)
            }

            @Override
            public void onRewardVideoCached(TTRewardVideoAd ttRewardVideoAd) {
                // 广告缓存成功 在此回调中进行广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 加载完成 %s", "激励广告", id, "csj", preload ? "预加载" : "");
                setRewardAdInteractionListener(ttRewardVideoAd, id);
                maps.put(id, ttRewardVideoAd);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }
        });
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        rewarded = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "csj");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        TTRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "激励广告", id, "csj");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "csj");
        // 展示激励视频
        ad.showRewardVideoAd(activity);
    }

    private void setRewardAdInteractionListener(TTRewardVideoAd ad, String id) {
        TTRewardVideoAd.RewardAdInteractionListener listen = new TTRewardVideoAd.RewardAdInteractionListener() {
            @Override
            public void onAdShow() {
                // 广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "激励广告", id, "csj");
                // 获取展示广告相关信息，需要再show回调之后进行获取
                MediationBaseManager manager = ad.getMediationManager();
                if (manager != null && manager.getShowEcpm() != null) {
                    MediationAdEcpmInfo showEcpm = manager.getShowEcpm();

                    JSONObject obj = new JSONObject();
                    String requestId = showEcpm.getRequestId();
                    String ecpm = showEcpm.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "rewardedVideoAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdVideoBarClick() {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s %s 广告点击", "激励广告", id, "csj");
            }

            @Override
            public void onAdClose() {
                // 广告关闭
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "csj");
                if (!rewarded) {
                    onRewardVideo(ad, id);
                    preload = true;
                    loadRewardVideoAd(id);
                }
            }

            @Override
            public void onVideoComplete() {
                // 广告视频播放完成
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "csj");
            }

            @Override
            public void onVideoError() {
                // 广告视频错误
                m_mainInstance.DebugPrintE("%s : %s %s 播放错误", "激励广告", id, "csj");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }

            @Override
            public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int errorCode, String errorMsg) {
                // 奖励发放 已废弃 请使用 onRewardArrived 替代
            }

            @Override
            public void onRewardArrived(boolean isRewardValid, int rewardType, Bundle extraInfo) {
                // 奖励发放
                reward = isRewardValid;
                m_mainInstance.DebugPrintE("%s : %s %s 奖励%s获得", "激励广告", id, "csj", reward ? "已" : "未");
            }

            @Override
            public void onSkippedVideo() {
                // 广告跳过
                m_mainInstance.DebugPrintE("%s : %s %s 跳过了广告", "激励广告", id, "csj");
                if (!rewarded) {
                    onRewardVideo(ad, id);
                    preload = true;
                    loadRewardVideoAd(id);
                }
            }
        };

        ad.setRewardAdInteractionListener(listen);

        ad.setRewardPlayAgainInteractionListener(listen);
    }

    private void onRewardVideo(TTRewardVideoAd ad, String id) {
        if (rewarded) return;
        rewarded = true;
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "csj");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            MediationBaseManager manager = ad.getMediationManager();
            if (manager != null && manager.getShowEcpm() != null) {
                MediationAdEcpmInfo showEcpm = manager.getShowEcpm();
                String requestId = showEcpm.getRequestId();
                String ecpm = showEcpm.getEcpm();
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
            }
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));

        if (ad != null) {
            MediationRewardManager manager = ad.getMediationManager();
            if (manager != null) {
                manager.destroy();
            }
        }
    }
}
