package com.cocos.game.gdtAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.qq.e.ads.rewardvideo.RewardVideoAD;
import com.qq.e.ads.rewardvideo.RewardVideoADListener;
import com.qq.e.comm.pi.IBidding;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, RewardVideoAD> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "gdt");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "gdt");
            return m_adMainCallBack;
        }

        RewardVideoAD ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "gdt");
            loadRewardVideoAD(id, activity);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载 是否已经展示过:%s, 是否有效:%s", "激励广告", id, "gdt", ad.hasShown(), ad.isValid());
            if (ad.hasShown() || !ad.isValid()) {
                ad.loadAD();
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "激励广告", id, "gdt");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAD(String id, Activity activity) {
        maps.remove(id);
        RewardVideoAD ad = new RewardVideoAD(activity, id, new RewardVideoADListener() {
            @Override
            public void onADLoad() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载完成%s", "激励广告", id, "gdt", preload ? " 预加载" : "");
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
                RewardVideoAD ad = maps.get(id);
                if (ad != null) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(IBidding.EXPECT_COST_PRICE, ad.getECPM());
                    hashMap.put(IBidding.HIGHEST_LOSS_PRICE, Math.max(0, ad.getECPM() - 1));
                    ad.sendWinNotification(hashMap);
                }
            }

            @Override
            public void onVideoCached() {
            }

            @Override
            public void onADShow() {
                RewardVideoAD ad = maps.get(id);
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示: %s", "激励广告", id, "gdt", ad != null ? ad.getExtraInfo() : "ad is null");
                JSONObject obj = new JSONObject();
                if (ad != null && ad.getExtraInfo() != null) {
                    Object rid = ad.getExtraInfo().get("request_id");
                    String requestId = rid != null ? JSON.toJSONString(rid) : id;
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ad.getECPM());
                }
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onADExpose() {
            }

            @Override
            public void onReward(Map<String, Object> map) {
                m_mainInstance.DebugPrintE("%s : %s %s 奖励已经获得", "激励广告", id, "gdt");
                reward = true;
            }

            @Override
            public void onADClick() {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s %s 广告点击", "激励广告", id, "gdt");
            }

            @Override
            public void onVideoComplete() {
                // 广告视频播放完成
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "激励广告", id, "gdt");
            }

            @Override
            public void onADClose() {
                // 广告关闭
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "gdt");
                RewardVideoAD ad = maps.get(id);
                if (ad != null) {
                    // 1 完成、0 未完成
                    onRewardVideo(ad, id);
                }
                preload = true;
                loadRewardVideoAD(id, activity);
            }

            @Override
            public void onError(AdError adError) {
                // 广告视频错误
                m_mainInstance.DebugPrintE("%s : %s %s 视频错误, code:%s, msg:%s", "激励广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, adError.getErrorCode(), adError.getErrorMsg());
                }
                maps.remove(id);
            }
        }, false);
        maps.put(id, ad);
        ad.loadAD();
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "gdt");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        RewardVideoAD ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "激励广告", id, "gdt");
            return;
        }

        showRewardVideo(ad, id);
    }

    private void showRewardVideo(RewardVideoAD ad, String id) {
        if (ad.hasShown()) {
            // 此条广告已经展示过，请再次请求广告后进行广告展示！
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示过", "激励广告", id, "gdt");
            return;
        }

        if (!ad.isValid()) {
            // 此条广告已经过期，请再次请求广告后进行广告展示！
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经过期", "激励广告", id, "gdt");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "gdt");
        ad.showAD();
    }

    private void onRewardVideo(RewardVideoAD ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "gdt");
        obj.put("adv_id", id);
        if (reward && ad != null && ad.getExtraInfo() != null) {
            Object rid = ad.getExtraInfo().get("request_id");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ad.getECPM());
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
