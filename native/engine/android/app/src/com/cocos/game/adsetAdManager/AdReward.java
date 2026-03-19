package com.cocos.game.adsetAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kc.openset.ad.listener.OSETRewardAdLoadListener;
import com.kc.openset.ad.listener.OSETRewardListener;
import com.kc.openset.ad.reward.OSETRewardAd;
import com.kc.openset.ad.reward.OSETRewardVideo;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, OSETRewardAd> maps = new HashMap<>();

    public static AdReward getInstance() {
        if (instance == null) {
            instance = new AdReward();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        reward = false;
        preload = false;

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "adset");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "adset");
            return m_adMainCallBack;
        }

        OSETRewardAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "adset");
            loadRewardVideoAd(id);
        } else {
            if (ad.isUsable()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "激励广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "adset");
                loadRewardVideoAd(id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        OSETRewardVideo.getInstance()
                .setContext((Activity) m_mainInstance.getGameCtx())
                .setPosId(id)
                .loadAd(new OSETRewardAdLoadListener() {
                    @Override
                    public void onLoadSuccess(OSETRewardAd osetRewardAd) {
                        maps.put(id, osetRewardAd);
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "adset", preload ? " 预加载" : "");
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String msg) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "激励广告", id, "adset", preload ? " 预加载" : "", code, msg);
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            try {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, Integer.parseInt(code), msg);
                            } catch (NumberFormatException e) {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, msg);
                            }
                        }
                    }
                });
    }

    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "adset");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        OSETRewardAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isUsable()) {
            m_mainInstance.DebugPrintE("%s : %s %s (act == null || ad %s null || !ad.isUsable) ", "激励广告", id, "adset", (ad == null) ? "==" : "!=");
            return;
        }

        OSETRewardAd finalAd = ad;
        ad.showAd((Activity) m_mainInstance.getGameCtx(), new OSETRewardListener() {
            @Override
            public void onClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告点击", "激励广告", id, "adset");
            }

            @Override
            public void onClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "adset");
                onRewardVideo(finalAd, id);
                finalAd.destroy();
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onReward() {
                reward = true;
                m_mainInstance.DebugPrintE("%s : %s %s 奖励已获得", "激励广告", id, "adset");
            }

            @Override
            public void onServiceResponse(int i) {
            }

            @Override
            public void onShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "激励广告", id, "adset");

                // 广告ecpm，单位:千次分
                // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
                JSONObject obj = new JSONObject();
                String requestId = finalAd.getRequestId();
                String ecpm = String.valueOf(finalAd.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onVideoEnd() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告视频播放完成", "激励广告", id, "adset");
            }

            @Override
            public void onVideoStart() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告视频播放开始", "激励广告", id, "adset");
            }

            @Override
            public void onError(String code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告视频错误", "激励广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    try {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, Integer.parseInt(code), msg);
                    } catch (NumberFormatException e) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, msg);
                    }
                }
            }
        });
    }

    private void onRewardVideo(OSETRewardAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "adset");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            // 广告ecpm，单位:千次分
            // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
            String requestId = ad.getRequestId();
            String ecpm = String.valueOf(ad.getECPM());
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
