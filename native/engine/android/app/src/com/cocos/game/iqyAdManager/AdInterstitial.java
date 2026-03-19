package com.cocos.game.iqyAdManager;

import android.app.Activity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.mcto.sspsdk.IQYNative;
import com.mcto.sspsdk.IQyFullScreenAd;
import com.mcto.sspsdk.QyAdSlot;
import com.mcto.sspsdk.QySdk;

import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private IQyFullScreenAd ad;
    private AdMainCallBack m_adMainCallBack;

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        IQYNative iQyNative = QySdk.getAdClient().createAdNative(m_mainInstance.getGameCtx());
        // 设置广告位属性
        QyAdSlot slot = QyAdSlot.newQyAdSlot()
                // 广告位ID
                .codeId(id)
                // 设置静音，默认非静音
                .isMute(true)
                .build();

        iQyNative.loadFullScreenAd(slot, new IQYNative.FullScreenAdListener() {
            @Override
            public void onFullScreenAdLoad(IQyFullScreenAd iQyFullScreenAd) {
                m_mainInstance.DebugPrintE("%s : %s 加载成功", "插屏广告", "iqy");
                ad = iQyFullScreenAd;
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s 加载失败 code:%s msg:%s", "插屏广告", "iqy", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }
        });

        return m_adMainCallBack;
    }

    // 展示插屏广告
    public void ShowAd(String id) {
        Activity activity = (Activity) AdMain.getInstance().getGameCtx();
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s act == null || ad == null", "插屏广告", "iqy");
            return;
        }

        ad.setAdInteractionListener(new IQyFullScreenAd.AdInteractionListener() {
            @Override
            public void onAdShow() {
                Map<String, String> adExtra = ad.getAdExtra();
                m_mainInstance.DebugPrintE("%s : %s 播放开始, adExtra=[%s]", "插屏广告", "iqy", JSON.toJSONString(adExtra));

                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s 播放点击", "插屏广告", "iqy");
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s 播放错误 code:%s msg:%s", "插屏广告", "iqy", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s 播放关闭", "插屏广告", "iqy");
            }

            @Override
            public void onVideoComplete() {
                m_mainInstance.DebugPrintE("%s : %s 播放完成", "插屏广告", "iqy");
            }
        });

        // 展示插屏广告
        ad.showAd(activity);
    }
}
