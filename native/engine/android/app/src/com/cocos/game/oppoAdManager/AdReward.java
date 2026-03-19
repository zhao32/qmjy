package com.cocos.game.oppoAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.heytap.msp.mobad.api.ad.RewardVideoAd;
import com.heytap.msp.mobad.api.listener.IRewardVideoAdListener;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, RewardVideoAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "oppo");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "oppo");
            return m_adMainCallBack;
        }

        RewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "oppo");
            loadRewardVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "激励广告", id, "oppo");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        RewardVideoAd ad = new RewardVideoAd(m_mainInstance.getGameCtx(), id, new IRewardVideoAdListener() {
            @Override
            public void onAdSuccess() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "oppo", preload ? "预加载" : "");
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onAdFailed(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s msg:%s", "激励广告", id, "oppo", preload ? "预加载" : "", s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, s);
                }
                maps.remove(id);
            }

            @Override
            public void onAdFailed(int i, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "oppo", preload ? "预加载" : "", i, s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, i, s);
                }
            }

            @Override
            public void onAdClick(long currentPosition) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "激励广告", id, "oppo");
            }

            @Override
            public void onVideoPlayStart() {
                m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "oppo");
                JSONObject obj = new JSONObject();
                String requestId = "";
                String ecpm = "";
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onVideoPlayComplete() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "oppo");
            }

            @Override
            public void onVideoPlayError(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放失败 msg:%s", "激励广告", id, "oppo", s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, s);
                }
            }

            @Override
            public void onVideoPlayClose(long l) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放关闭", "激励广告", id, "oppo");
                RewardVideoAd ad = maps.get(id);
                if (ad != null) {
                    onRewardVideo(ad, id);
                    ad.destroyAd();
                }
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onLandingPageOpen() {
                m_mainInstance.DebugPrintE("%s : %s %s 落地页打开", "激励广告", id, "oppo");
            }

            @Override
            public void onLandingPageClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 落地页关闭", "激励广告", id, "oppo");
            }

            @Override
            public void onReward(Object... objects) {
                m_mainInstance.DebugPrintE("%s : %s %s 获得奖励", "激励广告", id, "oppo");
                if (!reward) reward = true;
            }
        });
        maps.put(id, ad);
        ad.loadAd();
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "oppo");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        RewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !ad.isReady", "激励广告", id, "oppo");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "oppo");
        // 展示激励视频
        ad.showAd();
    }

    private void onRewardVideo(RewardVideoAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "oppo");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            String requestId = "";
            String ecpm = "";
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
