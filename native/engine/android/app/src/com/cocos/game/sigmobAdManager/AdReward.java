package com.cocos.game.sigmobAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardInfo;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;
import com.sigmob.windad.rewardVideo.WindRewardVideoAdListener;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private boolean reward = false;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, Object> options = new HashMap<>();
    private final Map<String, WindRewardAdRequest> requests = new HashMap<>();
    private final Map<String, WindRewardVideoAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "激励广告", id, "sigmob");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "激励广告", id, "sigmob");
            return m_adMainCallBack;
        }

        WindRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "sigmob");
            loadRewardVideoAd(id);
        } else {
            if (ad.isReady()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "激励广告", id, "sigmob");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "激励广告", id, "sigmob");
                ad.loadAd();
            }
        }

        return m_adMainCallBack;
    }

    private void loadRewardVideoAd(String id) {
        maps.remove(id);
        WindRewardAdRequest request = null;
        if (requests.containsKey(id)) {
            request = requests.get(id);
        }
        if (request == null) {
            // placementId 必填；user_id、options可不填，
            request = new WindRewardAdRequest(id, null, options);
            requests.put(id, request);
        }
        WindRewardVideoAd ad = new WindRewardVideoAd(request);
        ad.setWindRewardVideoAdListener(new WindRewardVideoAdListener() {
            @Override
            public void onRewardAdLoadSuccess(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "激励广告", id, "sigmob", preload ? " 预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onRewardAdLoadError(WindAdError windAdError, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载错误 %s, code:%s, msg:%s", "激励广告", id, "sigmob", preload ? " 预加载" : "", windAdError.getErrorCode(), windAdError.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, windAdError.getErrorCode(), windAdError.getMessage());
                }
            }

            @Override
            public void onRewardAdRewarded(WindRewardInfo windRewardInfo, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 奖励已经获得", "激励广告", id, "sigmob");
                reward = windRewardInfo.isReward();
            }

            @Override
            public void onRewardAdClosed(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "激励广告", id, "sigmob");
                WindRewardVideoAd ad = maps.get(id);
                // 1 完成、0 未完成
                onRewardVideo(ad, id);
                preload = true;
                loadRewardVideoAd(id);
            }

            @Override
            public void onRewardAdPreLoadSuccess(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 填充成功", "激励广告", id, "sigmob");
            }

            @Override
            public void onRewardAdPreLoadFail(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 填充失败", "激励广告", id, "sigmob");
            }

            @Override
            public void onRewardAdPlayStart(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放开始", "激励广告", id, "sigmob");
                WindRewardVideoAd ad = maps.get(id);
                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                if (ad != null) {
                    String ecpm = ad.getEcpm();
                    obj.put("adv_ecpm", ecpm);
                }
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onRewardAdPlayEnd(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放结束", "激励广告", id, "sigmob");
            }

            @Override
            public void onRewardAdClicked(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "激励广告", id, "sigmob");
            }

            @Override
            public void onRewardAdPlayError(WindAdError windAdError, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 播放错误, code:%s, msg:%s", "激励广告", id, "sigmob", windAdError.getErrorCode(), windAdError.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, windAdError.getErrorCode(), windAdError.getMessage());
                }
            }
        });
        ad.loadAd();
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        if (AdManager.getInstance().getRewardState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "激励广告", id, "sigmob");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        WindRewardVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isReady", "激励广告", id, "sigmob");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "激励广告", id, "sigmob");
        HashMap<String, String> showOption = new HashMap<>();
        ad.show(showOption);
    }

    private void onRewardVideo(WindRewardVideoAd ad, String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "sigmob");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            String ecpm = ad.getEcpm();
            obj.put("adv_no", id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
