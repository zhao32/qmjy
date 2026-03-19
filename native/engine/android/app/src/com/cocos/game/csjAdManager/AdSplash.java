package com.cocos.game.csjAdManager;

import android.graphics.Point;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.CSJAdError;
import com.bytedance.sdk.openadsdk.CSJSplashAd;
import com.bytedance.sdk.openadsdk.TTAdConstant;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationAdEcpmInfo;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationBaseManager;
import com.cocos.game.JsbBridgeCallback;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private CSJSplashAd ad;

    private AdMainCallBack m_adMainCallBack;

    // 获取&创建单例类
    public static AdSplash getInstance() {
        if (instance == null) {
            instance = new AdSplash();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        // 加载开屏广告
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Point screenSize = m_mainInstance.getScreen();
        AdSlot adSlot = new AdSlot.Builder()
                .setCodeId(id) // 广告位ID
                .setAdLoadType(TTAdLoadType.LOAD)
                .setOrientation(TTAdConstant.VERTICAL)
                .setImageAcceptedSize(screenSize.x, screenSize.y)  // 设置广告宽高 单位px
                .build();

        TTAdNative adNativeLoader = TTAdSdk.getAdManager().createAdNative(m_mainInstance.getGameCtx());
        adNativeLoader.loadSplashAd(adSlot, new TTAdNative.CSJSplashAdListener() {

            @Override
            public void onSplashLoadSuccess(CSJSplashAd csjSplashAd) {
                m_mainInstance.DebugPrintE("[%s] 广告加载成功", "开屏广告");
            }

            @Override
            public void onSplashLoadFail(CSJAdError csjAdError) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("[%s] 广告加载失败 Code: %s Msg: %s", "开屏广告", csjAdError.getCode(), csjAdError.getMsg());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, csjAdError, 0, null);
                }
            }

            @Override
            public void onSplashRenderSuccess(CSJSplashAd csjSplashAd) {
                // 广告渲染成功，在此展示广告
                m_mainInstance.DebugPrintE("[%s] 广告渲染成功", "开屏广告");

                ad = csjSplashAd;
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onSplashRenderFail(CSJSplashAd csjSplashAd, CSJAdError csjAdError) {
                if (csjAdError.getCode() == 602) {
                    m_mainInstance.DebugPrintE("[%s] Code: %s 请检查网络是否可以访问", "开屏广告", csjAdError.getCode());
                }
                //广告渲染失败
                m_mainInstance.DebugPrintE("[%s] 广告渲染失败 Code: %s msg: %s", "开屏广告", csjAdError.getCode(), csjAdError.getMsg());

                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, csjAdError.getCode(), csjAdError.getMsg());
                }
            }
        }, 3500);
        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd(String id) {
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("[%s] (ad %s null || container %s null) ", "开屏广告", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }

        ad.setSplashAdListener(new CSJSplashAd.SplashAdListener() {
            @Override
            public void onSplashAdShow(CSJSplashAd csjSplashAd) {
                // 广告展示
                m_mainInstance.DebugPrintE("[%s] 广告展示", "开屏广告");
                // 获取展示广告相关信息，需要再show回调之后进行获取
                MediationBaseManager manager = ad.getMediationManager();
                if (manager != null && manager.getShowEcpm() != null) {
                    MediationAdEcpmInfo showEcpm = manager.getShowEcpm();

                    JSONObject obj = new JSONObject();
                    String requestId = showEcpm.getRequestId();
                    String ecpm = showEcpm.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "splashAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onSplashAdClick(CSJSplashAd csjSplashAd) {
                // 广告点击
                m_mainInstance.DebugPrintE("[%s] 广告点击", "开屏广告");
            }

            @Override
            public void onSplashAdClose(CSJSplashAd csjSplashAd, int i) {
                // 广告关闭
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                removeAdView();
            }
        });

        removeAdView();
        container.addView(ad.getSplashView());
    }

    private void removeAdView() {
        if (ad != null) {
            View adView = ad.getSplashView();
            ViewGroup parent = (ViewGroup) adView.getParent();
            if (parent != null) parent.removeView(adView);
        }
    }

}
