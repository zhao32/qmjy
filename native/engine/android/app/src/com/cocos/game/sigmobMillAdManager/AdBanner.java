package com.cocos.game.sigmobMillAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.windmill.sdk.WMConstants;
import com.windmill.sdk.WindMillError;
import com.windmill.sdk.banner.WMBannerAdListener;
import com.windmill.sdk.banner.WMBannerAdRequest;
import com.windmill.sdk.banner.WMBannerView;
import com.windmill.sdk.models.AdInfo;

import java.util.HashMap;
import java.util.Map;

public class AdBanner {

    private static AdBanner instance;
    private AdMain m_mainInstance;
    private WMBannerView ad;
    private final Map<String, Object> options = new HashMap<>();
    private AdMainCallBack m_adMainCallBack;
    private int height = 0, top = 0;

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int height, int top) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("[%s] act == null || id == null", "banner广告");
            return m_adMainCallBack;
        }

        ad = new WMBannerView(activity);
        ad.setAutoAnimation(true);
        ad.setAdListener(new WMBannerAdListener() {
            @Override
            public void onAdLoadSuccess(String placementId) {
                m_mainInstance.DebugPrintE("[%s] 加载成功", "banner广告");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, null);
                }
            }

            @Override
            public void onAdLoadError(WindMillError error, String placementId) {
                // 广告加载或展示过程中出错
                m_mainInstance.DebugPrintE("[%s] 加载错误, code:%s, msg:%s", "banner广告", error.getErrorCode(), error.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, error.getErrorCode(), error.getMessage());
                }
            }

            @Override
            public void onAdShown(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 开始展示", "banner广告");
                JSONObject obj = new JSONObject();
                String requestId = adInfo.getLoadId();
                String ecpm = adInfo.geteCPM();
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClicked(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 被用户点击", "banner广告");
            }

            @Override
            public void onAdClosed(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告关闭", "banner广告");
            }

            @Override
            public void onAdAutoRefreshed(AdInfo adInfo) {
                m_mainInstance.DebugPrintE("[%s] 广告刷新", "banner广告");
            }

            @Override
            public void onAdAutoRefreshFail(WindMillError error, String placementId) {
                m_mainInstance.DebugPrintE("[%s] 刷新失败, code:%s, msg:%s", "banner广告", error.getErrorCode(), error.getMessage());
            }
        });

        Point screenSize = m_mainInstance.getScreen();
        int minHeight = Math.round(screenSize.x / 6.4F);
        this.height = Math.max(minHeight, height);
        this.top = top;
        options.put(WMConstants.AD_WIDTH, screenSize.x);
        options.put(WMConstants.AD_HEIGHT, this.height);

        // placementId 必填，user_id、options可不填，
        WMBannerAdRequest request = new WMBannerAdRequest(id, null, options);
        ad.loadAd(request);

        return m_adMainCallBack;
    }

    // 展示插全屏广告
    public void ShowAd() {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("[%s] act == null || container == null || ad == null || !isReady", "banner广告");
            return;
        }
        removeAdView();
        container.addView(ad, getLayoutParams(height, top));
    }

    private void removeAdView() {
        if (ad != null) {
            ViewGroup parent = (ViewGroup) ad.getParent();
            if (parent != null) parent.removeView(ad);
            ad.destroy();
        }
    }

    private FrameLayout.LayoutParams getLayoutParams(int height, int top) {
        Point screenSize = m_mainInstance.getScreen();
        int width = screenSize.x;
        int minHeight = Math.round(width / 6.4F);
        height = Math.max(minHeight, height);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        if (top > 0) {
            params.gravity = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
            params.topMargin = top;
        } else {
            params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
            params.bottomMargin = Math.abs(top);
        }
        return params;
    }
}
