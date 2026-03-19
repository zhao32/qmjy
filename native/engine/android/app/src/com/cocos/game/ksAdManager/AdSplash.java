package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.KsSplashScreenAd;
import com.kwad.sdk.api.model.SplashAdExtraData;

import java.util.Map;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private KsSplashScreenAd ad;

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

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "开屏广告", id, "ks");
            return m_adMainCallBack;
        }

        if (KsAdSDK.getLoadManager() != null) {
            SplashAdExtraData extraData = new SplashAdExtraData();
            extraData.setDisableShakeStatus(false);
            KsScene scene = new KsScene.Builder(Long.parseLong(id)).setSplashExtraData(extraData).build();
            KsAdSDK.getLoadManager().loadSplashScreenAd(scene, new KsLoadManager.SplashScreenAdListener() {
                @Override
                public void onError(int code, String msg) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误, code:%s, msg:%s", "开屏广告", id, "ks", code, msg);
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                    }
                }

                @Override
                public void onRequestResult(int adNumber) {
                    m_mainInstance.DebugPrintE("%s : %s %s 广告视频请求成功, adNumber:%s", "开屏广告", id, "ks", adNumber);
                }

                @Override
                public void onSplashScreenAdLoad(@Nullable KsSplashScreenAd ksSplashScreenAd) {
                    ad = ksSplashScreenAd;
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                }
            });
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "开屏广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }

        return m_adMainCallBack;
    }

    // 显示开屏广告
    private View adView;

    public void ShowAd(String id) {
        FrameLayout container = m_mainInstance.getMainView();
        if (container == null || ad == null || !ad.isAdEnable()) {
            m_mainInstance.DebugPrintE("%s%s : %s %s act == null || ad == null || !ad.isAdEnable", "开屏广告", id, "ks");
            return;
        }
        container.removeAllViews();

        adView = ad.getView(m_mainInstance.getGameCtx(), new KsSplashScreenAd.SplashScreenAdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s%s : %s %s 被用户点击", "开屏广告", id, "ks");
            }

            @Override
            public void onAdShowError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s%s : %s %s 展示失败, code:%s, msg:%s", "开屏广告", id, "ks", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, msg);
                }
                container.removeView(adView);
            }

            @Override
            public void onAdShowEnd() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示结束", "开屏广告", id, "ks");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }

            @Override
            public void onAdShowStart() {
                m_mainInstance.DebugPrintE("%s 展示开始", "开屏广告");
            }

            @Override
            public void onSkippedAd() {
                m_mainInstance.DebugPrintE("%s : %s %s 跳过展示", "开屏广告", id, "ks");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }

            @Override
            public void onDownloadTipsDialogShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 开始下载", "开屏广告", id, "ks");
            }

            @Override
            public void onDownloadTipsDialogDismiss() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭下载", "开屏广告", id, "ks");
            }

            @Override
            public void onDownloadTipsDialogCancel() {
                m_mainInstance.DebugPrintE("%s : %s %s 取消下载", "开屏广告", id, "ks");
            }
        });
        ViewGroup parent = (ViewGroup) adView.getParent();
        if (parent != null) parent.removeView(adView);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        adView.setLayoutParams(lp);
        container.addView(adView);

        JSONObject obj = new JSONObject();
        Map<String, Object> extra = ad.getMediaExtraInfo();
        Object rid = extra.get("transId");
        String requestId = rid != null ? JSON.toJSONString(rid) : id;
        String ecpm = String.valueOf(ad.getECPM());
        obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
        obj.put("adv_ecpm", ecpm);
        obj.put("adv_event", "splashAd");
        JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
    }
}
