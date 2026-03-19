package com.cocos.game.iqyAdManager;

import android.app.Activity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.mcto.sspsdk.IQYNative;
import com.mcto.sspsdk.IQyRewardVideoAd;
import com.mcto.sspsdk.QyAdSlot;
import com.mcto.sspsdk.QySdk;

import java.util.HashMap;
import java.util.Map;

// 激励广告
public class AdReward {
    private static AdReward instance;
    private AdMain m_mainInstance;
    private IQyRewardVideoAd ad;
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
    public AdMainCallBack LoadAd(String id, long channel) {
        reward = false;

        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        IQYNative iQyNative = QySdk.getAdClient().createAdNative(m_mainInstance.getGameCtx());
        // 设置广告位属性
        QyAdSlot slot = QyAdSlot.newQyAwardAdSlot()
                // 广告位ID
                .codeId(id)
                // 频道id，接入方自定义的值
                .channelId(channel)
                // 激励视频素材方向
                .rewardVideoAdOrientation(IQyRewardVideoAd.ORIENTATION_PORTRAIT)
                // 设置静音，默认非静音
                .isMute(true)
                // 可用激励回调次数，不传默认只回调一次激励完成
                .setAvailableTimes(1)
                .build();

        iQyNative.loadRewardVideoAd(slot, new IQYNative.RewardVideoAdListener() {
            @Override
            public void onRewardVideoAdLoad(IQyRewardVideoAd iQyRewardVideoAd) {
                m_mainInstance.DebugPrintE("%s : %s 加载成功", "激励广告", "iqy");
                ad = iQyRewardVideoAd;
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s 加载失败 code:%s msg:%s", "激励广告", "iqy", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }
        });

        return m_adMainCallBack;
    }

    // 展示激励视频
    public void ShowAd(String id) {
        reward = false;
        Activity activity = (Activity) AdMain.getInstance().getGameCtx();
        if (activity == null || ad == null || !ad.isValid()) {
            m_mainInstance.DebugPrintE("%s : %s act == null || ad == null || !isValid", "激励广告", "iqy");
            return;
        }

        ad.setRewardVideoAdInteractionListener(new IQyRewardVideoAd.IAdInteractionListener() {
            @Override
            public void onAdShow() {
                Map<String, String> adExtra = ad.getAdExtra();
                m_mainInstance.DebugPrintE("%s : %s 播放开始, adExtra=[%s]", "激励广告", "iqy", JSON.toJSONString(adExtra));

                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                obj.put("adv_event", "rewardedVideoAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s 播放点击", "激励广告", "iqy");
            }

            @Override
            public void onRewardVerify(HashMap<String, Object> hashMap) {
                // 奖励发放
                reward = true;
                m_mainInstance.DebugPrintE("%s : %s 奖励%s获得", "激励广告", "iqy", reward ? "已" : "未");
            }

            @Override
            public void onAdClose() {
                // 广告关闭
                m_mainInstance.DebugPrintE("%s : %s 播放关闭", "激励广告", "iqy");
                onRewardVideo(id);
            }

            @Override
            public void onVideoComplete() {
                // 广告视频播放完成
                m_mainInstance.DebugPrintE("%s : %s 播放完成", "激励广告", "iqy");
            }

            @Override
            public void onVideoError(int i, String s) {
                // 广告视频错误
                m_mainInstance.DebugPrintE("%s : %s 播放错误", "激励广告", "iqy");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }

            @Override
            public void onAdNextShow() {
                // 广告下一个
                m_mainInstance.DebugPrintE("%s : %s 播放下一个", "激励广告", "iqy");
            }
        });

        // 展示激励视频
        ad.showRewardVideoAd(activity);
    }

    private void onRewardVideo(String id) {
        JSONObject obj = new JSONObject();
        obj.put("adv_status", reward ? 1 : 0);
        obj.put("adv_provider", "iqy");
        obj.put("adv_id", id);
        if (reward && ad != null) {
            obj.put("adv_no", id);
        }
        JsbBridgeCallback.getInstance().sendToScript("rewarded", JSON.toJSONString(obj));
    }
}
