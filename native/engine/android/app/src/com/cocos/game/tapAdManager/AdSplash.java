package com.cocos.game.tapAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapSplashAd;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private TapSplashAd ad;

    private AdMainCallBack m_adMainCallBack;

    // 获取&创建单例类
    public static AdSplash getInstance() {
        if (instance == null) {
            instance = new AdSplash();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, String uid) {
        m_mainInstance.DebugPrintE("%s : %s %s 开始加载", "开屏广告", id, "tap");
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        TapAdNative adNative = TapAdManager.get().createAdNative(m_mainInstance.getGameCtx());
        AdRequest adRequest = new AdRequest.Builder()
                .withSpaceId(Integer.parseInt(id))
                .withUserId(!TextUtils.isEmpty(uid) ? uid : "")
                .build();
        adNative.loadSplashAd(adRequest, new TapAdNative.SplashAdListener() {
            @Override
            public void onSplashAdLoad(TapSplashAd tapSplashAd) {
                m_mainInstance.DebugPrintE("[%s] 加载成功", "开屏广告");
                ad = tapSplashAd;
                ad.setSplashInteractionListener(getSplashAdInteractionListener(id, ad));
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int code, String msg) {
                m_mainInstance.DebugPrintE("[%s] 加载失败 Code: %s Msg: %s", "开屏广告", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                }
            }
        });
        return m_adMainCallBack;
    }

    private TapSplashAd.AdInteractionListener getSplashAdInteractionListener(String id, TapSplashAd ad) {
        return new TapSplashAd.AdInteractionListener() {
            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("[%s] 广告展示", "开屏广告");
            }

            @Override
            public void onAdValidShow() {
                m_mainInstance.DebugPrintE("[%s] 广告展示 Valid", "开屏广告");
                JSONObject obj = new JSONObject();
                String requestId = "";
                double ecpm = 0;
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdSkip() {
                m_mainInstance.DebugPrintE("[%s] 广告跳过", "开屏广告");
                if (ad != null) {
                    ad.dispose();
                    ad.destroyView();
                }
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
            }

            @Override
            public void onAdTimeOver() {
                m_mainInstance.DebugPrintE("[%s] 广告结束", "开屏广告");
                if (ad != null) {
                    ad.dispose();
                    ad.destroyView();
                }
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("[%s] 广告点击", "开屏广告");
            }
        };
    }

    // 显示开屏广告
    public void ShowAd(String id) {
        if (ad == null) {
            m_mainInstance.DebugPrintE("[%s] 开始展示 (ad == null) ", "开屏广告");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "开屏广告", id, "tap");
        ad.show((Activity) m_mainInstance.getGameCtx());
    }

}
