package com.cocos.game.csjAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFullScreenVideoAd;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationAdEcpmInfo;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationBaseManager;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private TTAdNative adNativeLoader;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, AdSlot> slots = new HashMap<>();
    private final Map<String, TTFullScreenVideoAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "csj");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "csj");
            return m_adMainCallBack;
        }

        TTFullScreenVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "csj");
            loadFullScreenVideoAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "插屏广告", id, "csj");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadFullScreenVideoAd(String id) {
        maps.remove(id);
        AdSlot adSlot = null;
        if (slots.containsKey(id)) {
            adSlot = slots.get(id);
        }
        if (adSlot == null) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(id)
                    .setAdLoadType(TTAdLoadType.LOAD)
                    .setOrientation(TTAdConstant.VERTICAL)
                    .setMediationAdSlot(new MediationAdSlot.Builder().setMuted(false).build())
                    .build();
            slots.put(id, adSlot);
        }
        if (adNativeLoader == null)
            adNativeLoader = TTAdSdk.getAdManager().createAdNative(m_mainInstance.getGameCtx());
        adNativeLoader.loadFullScreenVideoAd(adSlot, new TTAdNative.FullScreenVideoAdListener() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "插屏广告", id, "csj", preload ? "预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onFullScreenVideoAdLoad(TTFullScreenVideoAd ttFullScreenVideoAd) {
                // 广告加载成功
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "csj", preload ? "预加载" : "");
            }

            @Override
            public void onFullScreenVideoCached() {
                // 广告缓存成功 此api已经废弃，请使用onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd)
            }

            @Override
            public void onFullScreenVideoCached(TTFullScreenVideoAd ttFullScreenVideoAd) {
                // 广告缓存成功 在此回调中进行广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 加载完成 %s", "插屏广告", id, "csj", preload ? "预加载" : "");
                setFullScreenVideoAdInteractionListener(ttFullScreenVideoAd, id);
                maps.put(id, ttFullScreenVideoAd);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }
        });
    }

    // 展示插屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "csj");
            return;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        TTFullScreenVideoAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null", "插屏广告", id, "csj");
            return;
        }

        ad.showFullScreenVideoAd(activity); // 展示插屏广告
    }

    private void setFullScreenVideoAdInteractionListener(TTFullScreenVideoAd ad, String id) {
        ad.setFullScreenVideoAdInteractionListener(new TTFullScreenVideoAd.FullScreenVideoAdInteractionListener() {
            @Override
            public void onAdShow() {
                // 广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "插屏广告", id, "csj");
                // 获取展示广告相关信息，需要再show回调之后进行获取
                MediationBaseManager manager = ad.getMediationManager();
                if (manager != null && manager.getShowEcpm() != null) {
                    MediationAdEcpmInfo showEcpm = manager.getShowEcpm();

                    JSONObject obj = new JSONObject();
                    String requestId = showEcpm.getRequestId();
                    String ecpm = showEcpm.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "interstitialAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdVideoBarClick() {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s %s 广告点击", "插屏广告", id, "csj");
            }

            @Override
            public void onAdClose() {
                // 广告关闭
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "插屏广告", id, "csj");
                preload = true;
                loadFullScreenVideoAd(id);
            }

            @Override
            public void onVideoComplete() {
                // 广告视频播放完成
                m_mainInstance.DebugPrintE("%s : %s %s 播放完成", "插屏广告", id, "csj");
            }

            @Override
            public void onSkippedVideo() {
                // 广告跳过
                m_mainInstance.DebugPrintE("%s : %s %s 跳过了广告", "插屏广告", id, "csj");
            }
        });
    }
}
