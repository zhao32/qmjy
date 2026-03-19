package com.cocos.game.bdAdManager;

import android.app.Activity;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baidu.mobads.sdk.api.ExpressInterstitialAd;
import com.baidu.mobads.sdk.api.ExpressInterstitialListener;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, ExpressInterstitialAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "bd");
            return m_adMainCallBack;
        }

        ExpressInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "bd");
            loadInterstitialAd(id);
        } else {
            if (ad.isReady()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "插屏广告", id, "bd");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "bd");
                ad.load();
            }
        }
        return m_adMainCallBack;
    }

    private void loadInterstitialAd(String id) {
        maps.remove(id);
        ExpressInterstitialAd ad = new ExpressInterstitialAd(m_mainInstance.getGameCtx(), id);
        ad.setLoadListener(new ExpressInterstitialListener() {
            @Override
            public void onADLoaded() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "bd", preload ? " 预加载" : "");
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 被点击", "插屏广告", id, "bd");
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 被关闭", "插屏广告", id, "bd");
                preload = true;
                loadInterstitialAd(id);
            }

            @Override
            public void onAdFailed(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s %s 请求失败 %s code:%s msg:%s", "插屏广告", id, "bd", preload ? " 预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onNoAd(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s %s 无广告返回 %s code:%s msg:%s", "插屏广告", id, "bd", preload ? " 预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onADExposed() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光成功", "插屏广告", id, "bd");

                ExpressInterstitialAd ad = maps.get(id);
                if (ad != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("adv_no", id);
                    obj.put("adv_ecpm", ad.getECPMLevel());
                    obj.put("adv_event", "interstitialAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onADExposureFailed() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光失败", "插屏广告", id, "bd");
            }

            @Override
            public void onAdCacheSuccess() {
                m_mainInstance.DebugPrintE("%s : %s %s 素材缓存成功  %s", "插屏广告", id, "bd", preload ? " 预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onAdCacheFailed() {
                m_mainInstance.DebugPrintE("%s : %s %s 素材缓存失败 %s", "插屏广告", id, "bd", preload ? " 预加载" : "");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, "");
                }
            }

            @Override
            public void onLpClosed() {
                m_mainInstance.DebugPrintE("%s : %s %s 落地页关闭", "插屏广告", id, "bd");
            }
        });
        ad.load();
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "bd");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        ExpressInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isReady", "插屏广告", id, "bd");
            return;
        }
        ad.show();
    }

}
