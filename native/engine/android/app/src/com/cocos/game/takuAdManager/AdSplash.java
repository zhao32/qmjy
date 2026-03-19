package com.cocos.game.takuAdManager;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.anythink.core.api.ATAdConst;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATShowConfig;
import com.anythink.core.api.AdError;
import com.anythink.splashad.api.ATSplashAd;
import com.anythink.splashad.api.ATSplashAdExtraInfo;
import com.anythink.splashad.api.ATSplashAdListener;
import com.anythink.splashad.api.IATSplashEyeAd;
import com.cocos.game.JsbBridgeCallback;

import java.util.HashMap;
import java.util.Map;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private ATSplashAd ad;

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
        m_mainInstance.DebugPrintE("%s : %s %s 开始加载", "开屏广告", id, "taku");
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        ad = new ATSplashAd(m_mainInstance.getGameCtx(), id, new ATSplashAdListener() {
            @Override
            public void onAdLoaded(boolean isTimeout) {
                m_mainInstance.DebugPrintE("[%s] 加载成功", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    if (isTimeout) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                    } else {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                }
            }

            @Override
            public void onAdLoadTimeout() {
                m_mainInstance.DebugPrintE("[%s] 加载超时", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }

            @Override
            public void onNoAdError(AdError adError) {
                m_mainInstance.DebugPrintE("[%s] 加载失败 Code: %s Msg: %s", "开屏广告", adError.getCode(), adError.getDesc());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, adError, 0, null);
                }
            }

            @Override
            public void onAdShow(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告展示", "开屏广告");
                if (atAdInfo != null) {
                    JSONObject obj = new JSONObject();
                    String requestId = atAdInfo.getRequestId();
                    double ecpm = atAdInfo.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "splashAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdClick(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告点击", "开屏广告");
            }

            @Override
            public void onAdDismiss(ATAdInfo atAdInfo, ATSplashAdExtraInfo atSplashAdExtraInfo) {
                IATSplashEyeAd atSplashEyeAd = atSplashAdExtraInfo.getAtSplashEyeAd();
                if (atSplashEyeAd != null) atSplashEyeAd.destroy();
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                m_mainInstance.getMainView().removeAllViews();
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
            }
        }, 3000);

        Point screen = m_mainInstance.getScreen();
        Map<String, Object> localMap = new HashMap<>();
        // 穿山甲开屏广告支持传入宽高，单位px
        localMap.put(ATAdConst.KEY.AD_WIDTH, screen.x);
        localMap.put(ATAdConst.KEY.AD_HEIGHT, screen.y);
        ad.setLocalExtra(localMap);

        ad.loadAd();
        return m_adMainCallBack;
    }

    private ATShowConfig getATShowConfig() {
        ATShowConfig.Builder builder = new ATShowConfig.Builder();
        builder.scenarioId("splash_show");
        builder.showCustomExt("splash_custom_ext");
        return builder.build();
    }

    // 显示开屏广告
    public void ShowAd(String id) {
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || !ad.isAdReady() || container == null) {
            m_mainInstance.DebugPrintE("[%s] (ad %s null || !ad.isAdReady || container %s null) ", "开屏广告", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "开屏广告", id, "taku");
        container.setBackgroundColor(Color.argb(50, 0, 0, 0));
        ad.show((Activity) m_mainInstance.getGameCtx(), container, null, getATShowConfig());
    }

}
