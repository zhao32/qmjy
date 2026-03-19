package com.cocos.game.oppoAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.heytap.msp.mobad.api.ad.SplashAd;
import com.heytap.msp.mobad.api.listener.ISplashAdListener;
import com.heytap.msp.mobad.api.params.SplashAdParams;

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

    public AdMainCallBack LoadAd(String id, String title, String desc) {
        m_mainInstance.DebugPrintE("%s : %s %s 开始加载", "开屏广告", id, "oppo");
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        SplashAdParams splashAdParams = new SplashAdParams.Builder()
                .setFetchTimeout(3000)
                .setTitle(title).setDesc(desc)
                .build();
        ad = new SplashAd((Activity) m_mainInstance.getGameCtx(), id, new ISplashAdListener() {
            @Override
            public void onAdDismissed() {
                ad.destroyAd();
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
            }

            @Override
            public void onAdShow(String s) {
                m_mainInstance.DebugPrintE("[%s] 广告展示 msg:%s", "开屏广告", s);

                onShow(id);
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("[%s] 广告展示", "开屏广告");

                onShow(id);
            }

            private void onShow(String id) {
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "1");
                }

                JSONObject obj = new JSONObject();
                String requestId = "";
                String ecpm = "";
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdFailed(String s) {
                m_mainInstance.DebugPrintE("[%s] 加载失败 Msg: %s", "开屏广告", s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, s);
                }
            }

            @Override
            public void onAdFailed(int i, String s) {
                m_mainInstance.DebugPrintE("[%s] 加载失败 Code: %s Msg: %s", "开屏广告", i, s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, i, s);
                }
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("[%s] 广告点击", "开屏广告");
            }
        }, splashAdParams);
        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd(String id) {
    }

}
