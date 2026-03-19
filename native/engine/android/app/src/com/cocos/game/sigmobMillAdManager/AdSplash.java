package com.cocos.game.sigmobMillAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.models.AdInfo;
import com.windmill.sdk.splash.IWMSplashEyeAd;
import com.windmill.sdk.splash.WMSplashAd;
import com.windmill.sdk.splash.WMSplashAdListener;
import com.windmill.sdk.splash.WMSplashAdRequest;

import java.util.HashMap;
import java.util.Map;

public class AdSplash {

    private static AdSplash instance;

    private AdMain m_mainInstance;

    private WMSplashAd ad;
    private final Map<String, Object> options = new HashMap<>();

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
            m_mainInstance.DebugPrintE("[%s] act == null || id == null", "开屏广告");
            return m_adMainCallBack;
        }

        Point screen = m_mainInstance.getScreen();
        options.put(WMConstants.AD_WIDTH, screen.x);
        options.put(WMConstants.AD_HEIGHT, screen.y);

        // placementId 必填，user_id、options可不填，
        WMSplashAdRequest request = new WMSplashAdRequest(id, null, options);
        request.setDisableAutoHideAd(true);
        ad = new WMSplashAd(activity, request, new WMSplashAdListener() {
            @Override
            public void onSplashAdSuccessPresent(AdInfo adInfo) {
                // 开屏广告成功展示
                m_mainInstance.DebugPrintE("[%s] 展示成功", "开屏广告");
                JSONObject obj = new JSONObject();
                String requestId = adInfo.getLoadId();
                String ecpm = adInfo.geteCPM();
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "splashAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onSplashAdSuccessLoad(String placementId) {
                // 开屏广告成功加载
                m_mainInstance.DebugPrintE("[%s] 加载成功", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, null);
                }
            }

            @Override
            public void onSplashAdFailToLoad(WindMillError error, String placementId) {
                // 开屏广告展示失败
                m_mainInstance.DebugPrintE("[%s] 加载错误, code:%s, msg:%s", "开屏广告", error.getErrorCode(), error.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, error.getErrorCode(), error.getMessage());
                }
            }

            @Override
            public void onSplashAdClicked(AdInfo adInfo) {
                // 开屏广告被点击
                m_mainInstance.DebugPrintE("[%s] 被用户点击", "开屏广告");
            }

            @Override
            public void onSplashClosed(AdInfo adInfo, IWMSplashEyeAd splashEyeAd) {
                // 开屏广告关闭, 需要判断是否能进入主页面
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "开屏广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, "2");
                }
                m_mainInstance.getMainView().removeAllViews();
                ad.destroy();
            }
        });

        ad.loadAdOnly();

        return m_adMainCallBack;
    }

    // 显示开屏广告
    public void ShowAd() {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("[%s] act == null || container == null || ad == null || !isReady", "开屏广告");
            return;
        }
        ad.showAd(container);
    }
}
