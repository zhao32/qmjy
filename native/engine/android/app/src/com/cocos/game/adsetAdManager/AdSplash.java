package com.cocos.game.adsetAdManager;

import android.app.Activity;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.kc.openset.ad.listener.OSETSplashAdLoadListener;
import com.kc.openset.ad.listener.OSETSplashListener;
import com.kc.openset.ad.splash.OSETSplash;
import com.kc.openset.ad.splash.OSETSplashAd;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private OSETSplashAd ad;

    private AdMainCallBack m_adMainCallBack;

    public static AdSplash getInstance() {
        if (instance == null) {
            instance = new AdSplash();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        OSETSplash.getInstance()
                .setContext((Activity) m_mainInstance.getGameCtx())
                .setPosId(id)
                .loadAd(new OSETSplashAdLoadListener() {
                    @Override
                    public void onLoadSuccess(OSETSplashAd osetSplashAd) {
                        m_mainInstance.DebugPrintE("[%s] 广告加载成功", "开屏广告");
                        ad = osetSplashAd;
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String msg) {
                        m_mainInstance.DebugPrintE("[%s] 广告加载失败 Code: %s Msg: %s", "开屏广告", code, msg);
                        FrameLayout container = AdMain.getInstance().getMainView();
                        if (container != null) container.removeAllViews();
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            try {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, Integer.parseInt(code), msg);
                            } catch (NumberFormatException e) {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, msg);
                            }
                        }
                    }
                });
        return m_adMainCallBack;
    }

    public void ShowAd(String id) {
        FrameLayout container = AdMain.getInstance().getMainView();
        if (ad == null || !ad.isUsable() || container == null) {
            m_mainInstance.DebugPrintE("[%s] (ad %s null || !ad.isUsable || container %s null) ", "开屏广告", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }

        container.removeAllViews();
        ad.showAd((Activity) m_mainInstance.getGameCtx(), container, new OSETSplashListener() {
            @Override
            public void onAdDetailViewClosed() {
                m_mainInstance.DebugPrintE("[%s] 广告关闭 AdDetailView", "开屏广告");
            }

            @Override
            public void onClick() {
                m_mainInstance.DebugPrintE("[%s] 广告点击", "开屏广告");
            }

            @Override
            public void onClose() {
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                ad.destroy();
                container.removeAllViews();
            }

            @Override
            public void onShow() {
                m_mainInstance.DebugPrintE("[%s] 广告展示", "开屏广告");

                // 广告ecpm，单位:千次分
                // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
                JSONObject obj = new JSONObject();
                String requestId = ad.getRequestId();
                String ecpm = String.valueOf(ad.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onError(String code, String msg) {
                m_mainInstance.DebugPrintE("[%s] 广告展示失败", "开屏广告");
                container.removeAllViews();
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
