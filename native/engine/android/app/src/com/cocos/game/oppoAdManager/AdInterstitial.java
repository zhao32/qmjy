package com.cocos.game.oppoAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.heytap.msp.mobad.api.ad.InterstitialAd;
import com.heytap.msp.mobad.api.listener.IInterstitialAdListener;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, InterstitialAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "oppo");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "oppo");
            return m_adMainCallBack;
        }

        InterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "oppo");
            loadFullScreenVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "插屏广告", id, "oppo");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadFullScreenVideoAd(String id) {
        maps.remove(id);
        InterstitialAd ad = new InterstitialAd((Activity) m_mainInstance.getGameCtx(), id);
        ad.setAdListener(new IInterstitialAdListener() {
            @Override
            public void onAdReady() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "oppo", preload ? "预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onAdFailed(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s msg:%s", "插屏广告", id, "oppo", preload ? "预加载" : "", s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, s);
                }
            }

            @Override
            public void onAdFailed(int i, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "插屏广告", id, "oppo", preload ? "预加载" : "", i, s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, i, s);
                }
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "插屏广告", id, "oppo");
                ad.destroyAd();
                preload = true;
                loadFullScreenVideoAd(id);
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示成功", "插屏广告", id, "oppo");
                JSONObject obj = new JSONObject();
                String requestId = "";
                String ecpm = "";
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "插屏广告", id, "oppo");
            }
        });
        ad.loadAd();
    }

    // 展示插屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "oppo");
            return;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        InterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "插屏广告", id, "oppo");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "插屏广告", id, "oppo");
        ad.showAd();
    }
}
