package com.cocos.game.bdAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baidu.mobads.sdk.api.RewardVideoAd;
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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "bd");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "bd");
            return m_adMainCallBack;
        }

        RewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "bd");
            loadRewardVideoAd(id);
        } else {
            if (ad.isReady()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "激励广告", id, "bd");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "bd");
                ad.load();
            }
        }
        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        RewardVideoAd ad = new RewardVideoAd(m_mainInstance.getGameCtx(), id, new RewardVideoAd.RewardVideoAdListener() {
            @Override
            public void onAdShow() {
                // 广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "激励广告", id, "bd");
                RewardVideoAd ad = maps.get(id);
                if (ad != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("adv_no", id);
                    obj.put("adv_ecpm", ad.getECPMLevel());
                    obj.put("adv_event", "rewardedVideoAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdClick() {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s %s 广告点击", "激励广告", id, "bd");
            }

            @Override
            public void onAdClose(float playScale) {
                // 广告关闭
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "bd");
                RewardVideoAd ad = maps.get(id);
                onRewardVideo(ad, id);
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onAdFailed(String msg) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "bd", preload ? " 预加载" : "", 0, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, msg);
                }
                maps.remove(id);
            }

            @Override
            public void onVideoDownloadSuccess() {
                // 视频物料缓存成功
                m_mainInstance.DebugPrintE("%s : %s %s 缓存成功 %s", "激励广告", id, "bd", preload ? " 预加载" : "");
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onVideoDownloadFailed() {
                // 视频物料缓存失败
                m_mainInstance.DebugPrintE("%s : %s %s 缓存失败 %s", "激励广告", id, "bd", preload ? " 预加载" : "");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }

            @Override
            public void playCompletion() {
                // 播放完成
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "bd");
            }

            @Override
            public void onAdLoaded() {
                // 广告加载成功
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "bd", preload ? " 预加载" : "");
            }

            @Override
            public void onAdSkip(float playScale) {
                // 视频跳过
                m_mainInstance.DebugPrintE("%s : %s %s 视频跳过", "激励广告", id, "bd");
            }

            @Override
            public void onRewardVerify(boolean isRewardValid) {
                reward = isRewardValid;
                // 激励视频奖励
                m_mainInstance.DebugPrintE("%s : %s %s 奖励%s获得", "激励广告", id, "bd", reward ? "已" : "未");
            }
        });
        maps.put(id, ad);
        ad.load();
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "bd");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        RewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isReady", "激励广告", id, "bd");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "bd");
        ad.show();
    }

    private void onRewardVideo(RewardVideoAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "bd");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            obj.put("adv_no", id);
            obj.put("adv_ecpm", ad.getECPMLevel());
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
