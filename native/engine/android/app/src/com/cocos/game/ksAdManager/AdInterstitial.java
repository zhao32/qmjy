package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsInterstitialAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, KsScene> scenes = new HashMap<>();
    private final Map<String, KsInterstitialAd> maps = new HashMap<>();

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "ks");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "ks");
            return m_adMainCallBack;
        }

        KsInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "ks");
            loadInterstitialAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "插屏广告", id, "ks");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadInterstitialAd(String id) {
        maps.remove(id);
        if (KsAdSDK.getLoadManager() != null) {
            KsScene scene = null;
            if (scenes.containsKey(id)) {
                scene = scenes.get(id);
            }
            if (scene == null) {
                scene = new KsScene.Builder(Long.parseLong(id)).build();
                scenes.put(id, scene);
            }
            KsAdSDK.getLoadManager().loadInterstitialAd(scene, getInterstitialAdListener(id));
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "插屏广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }
    }

    // 展示插全屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "ks");
            return;
        }
        KsInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "插屏广告", id, "ks");
            return;
        }

        ad.showInterstitialAd(activity, null);

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

    @NonNull
    private KsLoadManager.InterstitialAdListener getInterstitialAdListener(String id) {
        return new KsLoadManager.InterstitialAdListener() {
            @Override
            public void onError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载错误, code:%s, msg:%s", "插屏广告", id, "ks", code, msg);
            }

            @Override
            public void onRequestResult(int adNumber) {
                m_mainInstance.DebugPrintE("%s : %s %s 视频请求成功, adNumber:%s", "插屏广告", id, "ks", adNumber);
            }

            @Override
            public void onInterstitialAdLoad(@Nullable List<KsInterstitialAd> list) {
                if (list != null && !list.isEmpty()) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "ks", preload ? "预加载" : "");
                    KsInterstitialAd ksInterstitialAd = list.get(0);
                    setAdInteractionListener(ksInterstitialAd, id);
                    maps.put(id, ksInterstitialAd);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                } else {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "插屏广告", id, "ks", preload ? "预加载" : "");
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                    }
                }
            }
        };
    }

    private void setAdInteractionListener(KsInterstitialAd ad, String id) {
        ad.setAdInteractionListener(new KsInterstitialAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "插屏广告", id, "ks");
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "插屏广告", id, "ks");
            }

            @Override
            public void onAdClosed() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "插屏广告", id, "ks");
                preload = true;
                loadInterstitialAd(id);
            }

            @Override
            public void onPageDismiss() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭-页面", "插屏广告", id, "ks");
            }

            @Override
            public void onVideoPlayError(int code, int extra) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示错误", "插屏广告", id, "ks");
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
            public void onSkippedAd() {
                m_mainInstance.DebugPrintE("%s : %s %s 跳过展示", "插屏广告", id, "ks");
            }
        });
    }

}
