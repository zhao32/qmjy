package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsFullScreenVideoAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.List;
import java.util.Map;

// 插屏广告
public class AdInterstitialFull {

    private static AdInterstitialFull instance;
    private AdMain m_mainInstance;
    private KsFullScreenVideoAd ad;
    private AdMainCallBack m_adMainCallBack;

    public static AdInterstitialFull getInstance() {
        if (instance == null) {
            instance = new AdInterstitialFull();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "ks");
            return m_adMainCallBack;
        }

        KsScene scene = new KsScene.Builder(Long.parseLong(id)).build();
        if (KsAdSDK.getLoadManager() != null) {
            KsAdSDK.getLoadManager().loadFullScreenVideoAd(scene, new KsLoadManager.FullScreenVideoAdListener() {
                @Override
                public void onError(int code, String msg) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误, code:%s, msg:%s", "插屏广告", id, "ks", code, msg);
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                    }
                }

                @Override
                public void onFullScreenVideoResult(@Nullable List<KsFullScreenVideoAd> list) {
                    m_mainInstance.DebugPrintE("%s : %s %s 广告视频请求成功", "插屏广告", id, "ks");
                }

                @Override
                public void onFullScreenVideoAdLoad(@Nullable List<KsFullScreenVideoAd> list) {
                    if (list != null && !list.isEmpty()) {
                        ad = list.get(0);
                        setAdListener(id);
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    } else {
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                        }
                    }
                }
            });
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "插屏广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }

        return m_adMainCallBack;
    }

    // 展示插全屏广告
    public void ShowAd(String id) {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null || !ad.isAdEnable()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !ad.isAdEnable", "插屏广告", id, "ks");
            return;
        }

        ad.showFullScreenVideoAd(activity, null);

        JSONObject obj = new JSONObject();
        Map<String, Object> extra = ad.getMediaExtraInfo();
        Object rid = extra.get("transId");
        String requestId = rid != null ? JSON.toJSONString(rid) : id;
        String ecpm = String.valueOf(ad.getECPM());
        obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
        obj.put("adv_ecpm", ecpm);
        obj.put("adv_event", "interstitialAd");
        JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
    }

    private void setAdListener(String id) {
        ad.setFullScreenVideoAdInteractionListener(new KsFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "插屏广告", id, "ks");
            }

            @Override
            public void onPageDismiss() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "插屏广告", id, "ks");
            }

            @Override
            public void onVideoPlayError(int code, int extra) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示失败, code:%s, msg:%s", "插屏广告", id, "ks", code, extra);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, String.valueOf(extra));
                }
            }

            @Override
            public void onVideoPlayEnd() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示结束", "插屏广告", id, "ks");
            }

            @Override
            public void onVideoPlayStart() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示开始", "插屏广告", id, "ks");
            }

            @Override
            public void onSkippedVideo() {
                m_mainInstance.DebugPrintE("%s : %s %s 跳过展示", "插屏广告", id, "ks");
            }
        });
    }

}
