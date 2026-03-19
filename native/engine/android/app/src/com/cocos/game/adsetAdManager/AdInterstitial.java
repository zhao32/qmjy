package com.cocos.game.adsetAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kc.openset.ad.intestitial.OSETInterstitial;
import com.kc.openset.ad.intestitial.OSETInterstitialAd;
import com.kc.openset.ad.listener.OSETInterstitialAdLoadListener;
import com.kc.openset.ad.listener.OSETInterstitialListener;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, OSETInterstitialAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "adset");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "adset");
            return m_adMainCallBack;
        }

        OSETInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "adset");
            loadInterstitialAd(id);
        } else {
            if (ad.isUsable()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "插屏广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "adset");
                loadInterstitialAd(id);
            }
        }
        return m_adMainCallBack;
    }

    private void loadInterstitialAd(String id) {
        maps.remove(id);
        OSETInterstitial.getInstance()
                .setContext((Activity) m_mainInstance.getGameCtx())
                .setPosId(id)
                .loadAd(new OSETInterstitialAdLoadListener() {
                    @Override
                    public void onLoadSuccess(OSETInterstitialAd osetInterstitialAd) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "adset", preload ? " 预加载" : "");
                        maps.put(id, osetInterstitialAd);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String msg) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "插屏广告", id, "adset", preload ? " 预加载" : "", code, msg);
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
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "adset");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        OSETInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isUsable()) {
            m_mainInstance.DebugPrintE("%s : %s %s (act == null || ad %s null || !ad.isUsable) ", "插屏广告", id, "adset", (ad == null) ? "==" : "!=");
            return;
        }

        OSETInterstitialAd finalAd = ad;
        ad.showAd((Activity) m_mainInstance.getGameCtx(), new OSETInterstitialListener() {
            @Override
            public void onClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "插屏广告", id, "adset");
            }

            @Override
            public void onClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "插屏广告", id, "adset");
                finalAd.destroy();
                preload = true;
                loadInterstitialAd(id);
            }

            @Override
            public void onShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "插屏广告", id, "adset");

                // 广告ecpm，单位:千次分
                // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
                JSONObject obj = new JSONObject();
                String requestId = finalAd.getRequestId();
                String ecpm = String.valueOf(finalAd.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onError(String code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示失败", "插屏广告", id, "adset");
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
}
