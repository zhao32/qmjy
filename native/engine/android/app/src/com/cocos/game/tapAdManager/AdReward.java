package com.cocos.game.tapAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapRewardVideoAd;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, TapRewardVideoAd> maps = new HashMap<>();

    public static AdReward getInstance() {
        if (instance == null) {
            instance = new AdReward();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    // 加载激励视频
    public AdMainCallBack LoadAd(String id, String uid) {
        reward = false;
        preload = false;

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "tap");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "tap");
            return m_adMainCallBack;
        }

        TapRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "tap");
            loadRewardVideoAd(id, uid);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "激励广告", id, "tap");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id, String uid) {
        maps.remove(id);
        TapAdNative adNative = TapAdManager.get().createAdNative(m_mainInstance.getGameCtx());
        AdRequest adRequest = new AdRequest.Builder()
                .withSpaceId(Integer.parseInt(id))
                .withUserId(!TextUtils.isEmpty(uid) ? uid : "")
                .build();
        adNative.loadRewardVideoAd(adRequest, new TapAdNative.RewardVideoAdListener() {
            @Override
            public void onRewardVideoAdLoad(TapRewardVideoAd ad) {
                // 获取广告成功，可以展示广告
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "tap", preload ? "预加载" : "");
                maps.put(id, ad);
                ad.setRewardAdInteractionListener(getRewardAdInteractionListener(id, uid, ad));
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onRewardVideoCached(TapRewardVideoAd ad) {
                // 获取广告素材成功，可以展示广告，如果没有选择在 onRewardVideoAdLoad 时展示广告。（更建议在这个回调中展示广告，体验更好）
                m_mainInstance.DebugPrintE("%s : %s %s 素材缓存成功 %s", "激励广告", id, "tap", preload ? "预加载" : "");
            }

            @Override
            public void onError(int code, String msg) {
                // 获取失败
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "tap", preload ? "预加载" : "", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                }
            }
        });
    }

    private TapRewardVideoAd.RewardAdInteractionListener getRewardAdInteractionListener(String id, String uid, TapRewardVideoAd ad) {
        return new TapRewardVideoAd.RewardAdInteractionListener() {
            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 开始播放", "激励广告", id, "tap");
            }

            @Override
            public void onAdValidShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 开始播放 Valid", "激励广告", id, "tap");
                JSONObject obj = new JSONObject();
                String requestId = "";
                double ecpm = 0;
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放关闭", "激励广告", id, "tap");
                onRewardVideo(ad, id);
                if (ad != null) ad.dispose();
                preload = true;
                loadRewardVideoAd(id, uid);
            }

            @Override
            public void onVideoComplete() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "tap");
            }

            @Override
            public void onVideoError() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放失败", "激励广告", id, "tap");
                if (ad != null) ad.dispose();
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }

            @Override
            public void onRewardVerify(boolean rewardVerify, int rewardAmount, String rewardName, int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 获得奖励", "激励广告", id, "tap");
                if (!reward) reward = true;
            }

            @Override
            public void onSkippedVideo() {
                m_mainInstance.DebugPrintE("%s : %s %s 跳过", "激励广告", id, "tap");
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "激励广告", id, "tap");
            }
        };
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "tap");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        TapRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "激励广告", id, "tap");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "tap");
        // 展示激励视频
        ad.showRewardVideoAd(activity);
    }

    private void onRewardVideo(TapRewardVideoAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "tap");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            String requestId = "";
            double ecpm = 0;
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
