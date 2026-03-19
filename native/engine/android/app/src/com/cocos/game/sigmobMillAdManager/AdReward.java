package com.cocos.game.sigmobMillAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.reward.WMRewardAd;
import com.windmill.sdk.reward.WMRewardAdListener;
import com.windmill.sdk.reward.WMRewardAdRequest;
import com.windmill.sdk.reward.WMRewardInfo;

import java.util.HashMap;
import java.util.Map;

public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private WMRewardAd ad;
    private final Map<String, Object> options = new HashMap<>();
    private boolean reward = false;
    private AdMainCallBack m_adMainCallBack;

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

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("[%s] act == null || id == null", "激励广告");
            return m_adMainCallBack;
        }

        // placementId 必填；user_id、options可不填，
        WMRewardAdRequest request = new WMRewardAdRequest(id, null, options);
        ad = new WMRewardAd(activity, request);
        ad.setRewardedAdListener(new WMRewardAdListener() {
            @Override
            public void onVideoAdLoadSuccess(String placementId) {
                m_mainInstance.DebugPrintE("[%s] 加载成功", "激励广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, null);
                }
            }

            @Override
            public void onVideoAdLoadError(WindMillError error, String placementId) {
                m_mainInstance.DebugPrintE("[%s] 加载错误, code:%s, msg:%s", "激励广告", error.getErrorCode(), error.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, error.getErrorCode(), error.getMessage());
                }
            }

            @Override
            public void onVideoAdPlayStart(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 播放开始", "激励广告");
                JSONObject obj = new JSONObject();
                String requestId = adInfo.getLoadId();
                String ecpm = adInfo.geteCPM();
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onVideoAdPlayError(WindMillError error, String placementId) {
                m_mainInstance.DebugPrintE("[%s] 播放错误, code:%s, msg:%s", "激励广告", error.getErrorCode(), error.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, error.getErrorCode(), error.getMessage());
                }
            }

            @Override
            public void onVideoAdPlayEnd(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 播放结束", "激励广告");
            }

            @Override
            public void onVideoAdClicked(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 被用户点击", "激励广告");
            }

            @Override
            public void onVideoAdClosed(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "激励广告");
                // 1 完成、0 未完成
                onRewardVideo(id, adInfo);
            }

            @Override
            public void onVideoRewarded(AdInfo adInfo, WMRewardInfo rewardInfo) {
                m_mainInstance.DebugPrintE("[%s] 奖励已经获得", "激励广告");
                reward = rewardInfo.isReward();
            }
        });

        ad.loadAd();

        return m_adMainCallBack;
    }

    // 展示激励视频
    public void ShowAd() {
        reward = false;
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("[%s] act == null || ad == null || !isReady", "激励广告");
            return;
        }
        HashMap<String, String> showOption = new HashMap<>();
        ad.show(activity, showOption);
    }

    private void onRewardVideo(String id, AdInfo adInfo) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "sigmobMill");
        obj.put("adv_id", id);
        if (reward && adInfo != null) {
            String requestId = adInfo.getLoadId();
            String ecpm = adInfo.geteCPM();
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
