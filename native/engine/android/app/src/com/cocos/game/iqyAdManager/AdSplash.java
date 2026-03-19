package com.cocos.game.iqyAdManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.mcto.sspsdk.IQYNative;
import com.mcto.sspsdk.IQySplash;
import com.mcto.sspsdk.QyAdSlot;
import com.mcto.sspsdk.QySdk;
import com.yourong.game.jydhm.R;

import java.util.Map;

// 开屏广告
public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private IQySplash ad;

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

        IQYNative iQyNative = QySdk.getAdClient().createAdNative(m_mainInstance.getGameCtx());
        // 设置广告位属性
        QyAdSlot slot = QyAdSlot.newQySplashAdSlot()
                // 是否支持开屏广告预请求功能，开启该功能时，SDK会提前预缓存少量（大约8个）开屏广告素材（图片 < 200KB 或 视频 < 2MB）
                .supportPreRequest(true)
                // 广告位id
                .codeId(id)
                // 开屏底部以及右上角的 logo
                .splashLogo(R.mipmap.ic_launcher)
                // 开屏广告请求超时时长，不设置的话，SDK默认3秒，开启预请求时建议不少于1.3秒，不开启预请求时建议不少于3秒
                .timeout(3000)
                .build();

        iQyNative.loadSplashAd(slot, new IQYNative.SplashAdListener() {
            @Override
            public void onTimeout() {
                m_mainInstance.DebugPrintE("%s : %s 加载超时", "开屏广告", "iqy");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }

            @Override
            public void onSplashAdLoad(IQySplash iQySplash) {
                m_mainInstance.DebugPrintE("%s : %s 加载成功", "开屏广告", "iqy");
                ad = iQySplash;
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s 加载失败 Code: %s Msg: %s", "开屏广告", "iqy", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }
        });
        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd(String id) {
        FrameLayout container = AdMain.getInstance().getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s (ad %s null || container %s null) ", "开屏广告", "iqy", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }

        container.removeAllViews();
        ad.setSplashInteractionListener(new IQySplash.IAdInteractionListener() {
            @Override
            public void onAdShow() {
                Map<String, String> adExtra = ad.getAdExtra();
                m_mainInstance.DebugPrintE("%s : %s 播放开始, adExtra=[%s]", "开屏广告", "iqy", JSON.toJSONString(adExtra));

                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s 广告点击", "开屏广告", "iqy");
            }

            @Override
            public void onAdSkip() {
                m_mainInstance.DebugPrintE("%s : %s 播放跳过", "开屏广告", "iqy");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                View adView = ad.getSplashView();
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }

            @Override
            public void onAdTimeOver() {
                m_mainInstance.DebugPrintE("%s : %s 播放结束", "开屏广告", "iqy");
                m_mainInstance.DebugPrintE("%s : %s 广告关闭", "开屏广告", "iqy");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                View adView = ad.getSplashView();
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }
        });

        // 建议在注册监听之后添加，防止收不到广告展示和点击事件回调
        View adView = ad.getSplashView();
        ViewGroup parent = (ViewGroup) adView.getParent();
        if (parent != null) parent.removeView(adView);
        container.addView(adView,
                new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT));
    }
}
