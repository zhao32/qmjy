package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsInnerAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsRewardVideoAd;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.model.KsExtraRewardType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, KsScene> scenes = new HashMap<>();
    private final Map<String, KsRewardVideoAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "ks");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "ks");
            return m_adMainCallBack;
        }

        KsRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "ks");
            loadRewardVideoAd(id);
        } else {
            if (ad.isAdEnable()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "激励广告", id, "ks");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "ks");
                loadRewardVideoAd(id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        if (KsAdSDK.getLoadManager() != null) {
            KsScene scene = null;
            if (scenes.containsKey(id)) {
                scene = scenes.get(id);
            }
            if (scene == null) {
                scene = new KsScene.Builder(Long.parseLong(id)).build();
                scenes.put(id, scene);
            }
            KsAdSDK.getLoadManager().loadRewardVideoAd(scene, getRewardVideoAdListener(id));
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误%s, msg:%s", "激励广告", id, "ks", preload ? " 预加载" : "", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }
    }

    @NonNull
    private KsLoadManager.RewardVideoAdListener getRewardVideoAdListener(String id) {
        return new KsLoadManager.RewardVideoAdListener() {
            @Override
            public void onError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载错误%s, code:%s, msg:%s", "激励广告", id, "ks", preload ? " 预加载" : "", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                }
            }

            @Override
            public void onRewardVideoResult(@Nullable List<KsRewardVideoAd> list) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功%s", "激励广告", id, "ks", preload ? " 预加载" : "");
            }

            @Override
            public void onRewardVideoAdLoad(@Nullable List<KsRewardVideoAd> list) {
                if (list != null && !list.isEmpty()) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载完成%s", "激励广告", id, "ks", preload ? " 预加载" : "");
                    KsRewardVideoAd ad = list.get(0);
                    maps.put(id, ad);
                    setRewardAdInteractionListener(ad, id);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                } else {
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                    }
                }
            }
        };
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "ks");
            return;
        }
        KsRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null || !ad.isAdEnable()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !ad.isAdEnable", "激励广告", id, "ks");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "ks");
        ad.showRewardVideoAd(activity, null);
    }

    private void setRewardAdInteractionListener(KsRewardVideoAd ad, String id) {
        ad.setRewardAdInteractionListener(new KsRewardVideoAd.RewardAdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "激励广告", id, "ks");
            }

            @Override
            public void onPageDismiss() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "ks");
                onRewardVideo(ad, id);
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onVideoPlayError(int code, int extra) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示失败, code:%s, msg:%s", "激励广告", id, "ks", code, extra);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, String.valueOf(extra));
                }
            }

            @Override
            public void onVideoPlayEnd() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示结束", "激励广告", id, "ks");
            }

            @Override
            public void onVideoSkipToEnd(long l) {
                m_mainInstance.DebugPrintE("%s : %s %s 跳过展示", "激励广告", id, "ks");
            }

            @Override
            public void onVideoPlayStart() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示开始", "激励广告", id, "ks");
                JSONObject obj = new JSONObject();
                if (ad.getMediaExtraInfo() != null) {
                    Map<String, Object> extra = ad.getMediaExtraInfo();
                    Object rid = extra.get("transId");
                    String requestId = rid != null ? JSON.toJSONString(rid) : id;
                    String ecpm = String.valueOf(ad.getECPM());
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                }
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onRewardVerify() {
                m_mainInstance.DebugPrintE("%s : %s %s 获取奖励", "激励广告", id, "ks");
                reward = true;
            }

            @Override
            public void onRewardVerify(Map<String, Object> map) {
                m_mainInstance.DebugPrintE("%s : %s %s 获取奖励: %s", "激励广告", id, "ks", JSON.toJSONString(map));
                reward = true;
            }

            @Override
            public void onRewardStepVerify(int taskType, int taskStatus) {
                m_mainInstance.DebugPrintE("%s : %s %s 获取奖励-分阶段, type:%s, status:%s", "激励广告", id, "ks", taskType, taskStatus);
            }

            @Override
            public void onExtraRewardVerify(@KsExtraRewardType int extra) {
                m_mainInstance.DebugPrintE("%s : %s %s 获取额外奖励, extra:%s", "激励广告", id, "ks", extra);
            }
        });
        ad.setInnerAdInteractionListener(new KsInnerAd.KsInnerAdInteractionListener() {
            @Override
            public void onAdClicked(KsInnerAd ksInnerAd) {
                m_mainInstance.DebugPrintE("%s : %s %s 内部广告点击, type:%s", "激励广告", id, "ks", ksInnerAd.getType());
            }

            @Override
            public void onAdShow(KsInnerAd ksInnerAd) {
                m_mainInstance.DebugPrintE("%s : %s %s 内部广告曝光, type:%s", "激励广告", id, "ks", ksInnerAd.getType());
            }
        });
    }

    private void onRewardVideo(KsRewardVideoAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "ks");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            Map<String, Object> extra = ad.getMediaExtraInfo();
            Object rid = extra.get("transId");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            String ecpm = String.valueOf(ad.getECPM());
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
