package com.cocos.game.bdAdManager;

import android.graphics.Point;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.baidu.mobads.sdk.api.SplashAd;
import com.baidu.mobads.sdk.api.SplashInteractionListener;
import com.cocos.game.JsbBridgeCallback;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private SplashAd ad;

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
        RequestParameters.Builder parameters = new RequestParameters.Builder()
                // sdk 内部默认超时时间为 4200，单位：毫秒
                .addExtra(SplashAd.KEY_TIMEOUT, "4200")
                // sdk 内部默认值为 true
                .addExtra(SplashAd.KEY_DISPLAY_DOWNLOADINFO, "true")
                // 用户点击开屏下载类广告时，是否弹出 Dialog
                // 此选项设置为true的情况下，会覆盖掉 {SplashAd.KEY_DISPLAY_DOWNLOADINFO} 的设置
                .addExtra(SplashAd.KEY_POPDIALOG_DOWNLOAD, "true")
                // 设置开屏图片宽高(单位 dp)，建议按照 app 情况传入正确的宽高比例值
                .setWidth(screenSize.x)
                .setHeight(screenSize.y);

        ad = new SplashAd(m_mainInstance.getGameCtx(), id, parameters.build(), new SplashInteractionListener() {
            @Override
            public void onLpClosed() {
                m_mainInstance.DebugPrintE("[%s] 广告落地页关闭", "开屏广告");

                if (ad != null) ad.destroy();
            }

            @Override
            public void onAdPresent() {
                m_mainInstance.DebugPrintE("[%s] 广告成功展示", "开屏广告");

                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                obj.put("adv_ecpm", ad.getECPMLevel());
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdExposed() {
                m_mainInstance.DebugPrintE("[%s] 广告渲染成功", "开屏广告");
            }

            @Override
            public void onAdDismissed() {
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }

                if (ad != null) ad.destroy();
                m_mainInstance.getMainView().removeAllViews();
            }

            @Override
            public void onAdSkip() {
                m_mainInstance.DebugPrintE("[%s] 广告点击跳过", "开屏广告");
                if (ad != null) ad.destroy();
                m_mainInstance.getMainView().removeAllViews();
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("[%s] 广告被点击", "开屏广告");
            }

            @Override
            public void onAdCacheSuccess() {
                // 广告渲染成功，在此展示广告
                m_mainInstance.DebugPrintE("[%s] 广告物料缓存成功", "开屏广告");

                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onAdCacheFailed() {
                // 广告渲染失败
                m_mainInstance.DebugPrintE("[%s] 广告物料缓存失败 Code: %s msg: %s", "开屏广告", 0, "");

                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "");
                }

                if (ad != null) ad.destroy();
                m_mainInstance.getMainView().removeAllViews();
            }

            @Override
            public void onADLoaded() {
                m_mainInstance.DebugPrintE("[%s] 广告请求成功", "开屏广告");
            }

            @Override
            public void onAdFailed(String msg) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("[%s] 广告加载失败 Code: %s Msg: %s", "开屏广告", 0, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }

                if (ad != null) ad.destroy();
                m_mainInstance.getMainView().removeAllViews();
            }
        });
        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd() {
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("[%s] (ad %s null || container %s null) ", "开屏广告", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }
        container.removeAllViews();
        ad.loadAndShow(container);
    }

}
