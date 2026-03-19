package com.cocos.game.tapAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapInterstitialAd;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, TapInterstitialAd> maps = new HashMap<>();

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, String uid) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "tap");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "tap");
            return m_adMainCallBack;
        }

        TapInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "tap");
            loadFullScreenVideoAd(id, uid);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "插屏广告", id, "tap");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadFullScreenVideoAd(String id, String uid) {
        maps.remove(id);
        TapAdNative adNative = TapAdManager.get().createAdNative(m_mainInstance.getGameCtx());
        AdRequest adRequest = new AdRequest.Builder()
                .withSpaceId(Integer.parseInt(id))
                .withUserId(!TextUtils.isEmpty(uid) ? uid : "")
                .build();
        adNative.loadInterstitialAd(adRequest, new TapAdNative.InterstitialAdListener() {
            @Override
            public void onInterstitialAdLoad(TapInterstitialAd ad) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "tap", preload ? "预加载" : "");
                maps.put(id, ad);
                ad.setInteractionListener(getInterstitialAdInteractionListener(id, uid, ad));
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "插屏广告", id, "tap", preload ? "预加载" : "", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                }
            }
        });
    }

    private TapInterstitialAd.InterstitialAdInteractionListener getInterstitialAdInteractionListener(String id, String uid, TapInterstitialAd ad) {
        return new TapInterstitialAd.InterstitialAdInteractionListener() {
            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示成功", "插屏广告", id, "tap");
            }

            @Override
            public void onAdValidShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示成功 Valid", "插屏广告", id, "tap");
                JSONObject obj = new JSONObject();
                String requestId = "";
                double ecpm = 0;
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "插屏广告", id, "tap");
                if (ad != null) ad.dispose();
                preload = true;
                loadFullScreenVideoAd(id, uid);
            }

            @Override
            public void onAdError() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放失败", "插屏广告", id, "tap");
                if (ad != null) ad.dispose();
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }
        };
    }

    // 展示插屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "tap");
            return;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        TapInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "插屏广告", id, "tap");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "插屏广告", id, "tap");
        ad.show(activity);
    }
}
