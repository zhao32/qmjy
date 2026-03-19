package com.cocos.game.takuAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.anythink.banner.api.ATBannerListener;
import com.anythink.banner.api.ATBannerView;
import com.anythink.core.api.ATAdInfo;
import com.anythink.core.api.ATShowConfig;
import com.anythink.core.api.AdError;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, ATBannerView> maps = new HashMap<>();

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int height, int top) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "taku");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || id == null", "banner广告", id, "taku");
            return m_adMainCallBack;
        }

        ATBannerView ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "taku");
            loadBannerExpressAd(id, height, top);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "banner广告", id, "taku");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }
        return m_adMainCallBack;
    }

    private void loadBannerExpressAd(String id, int height, int top) {
        maps.remove(id);
        ATBannerView ad = new ATBannerView(m_mainInstance.getGameCtx());
        ad.setShowConfig(getATShowConfig());
        ad.setBannerAdListener(new ATBannerListener() {
            @Override
            public void onBannerLoaded() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "taku", preload ? "预加载" : "");
                maps.put(id, ad);

                removeAdView(ad);
                FrameLayout container = m_mainInstance.getMainView();
                container.addView(ad, getLayoutParams(height, top));
                ad.setVisibility(View.GONE);

                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onBannerFailed(AdError adError) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s adError:%s", "banner广告", id, "taku", preload ? "预加载" : "", JSON.toJSONString(adError));
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }

            @Override
            public void onBannerClicked(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "banner广告", id, "taku");
            }

            @Override
            public void onBannerShow(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "banner广告", id, "taku");
                onShow(atAdInfo, id);
            }

            @Override
            public void onBannerClose(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "banner广告", id, "taku");
                removeAdView(ad);
                preload = true;
                loadBannerExpressAd(id, height, top);
            }

            @Override
            public void onBannerAutoRefreshed(ATAdInfo atAdInfo) {
                m_mainInstance.DebugPrintE("%s : %s %s 刷新成功", "banner广告", id, "taku");
                onShow(atAdInfo, id);
            }

            private void onShow(ATAdInfo atAdInfo, String id) {
                if (atAdInfo != null) {
                    JSONObject obj = new JSONObject();
                    String requestId = atAdInfo.getRequestId();
                    double ecpm = atAdInfo.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "bannerAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onBannerAutoRefreshFail(AdError adError) {
                m_mainInstance.DebugPrintE("%s : %s %s 刷新失败", "banner广告", id, "taku");
            }
        });
        ad.loadAd();
    }

    private ATShowConfig getATShowConfig() {
        ATShowConfig.Builder builder = new ATShowConfig.Builder();
        builder.scenarioId("banner_show");
        builder.showCustomExt("banner_custom_ext");
        return builder.build();
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "banner广告", id, "taku");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        ATBannerView ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || container == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView act == null || container == null || ad == null", "banner广告", id, "taku");
            return;
        }

        ad.setVisibility(View.VISIBLE);
    }

    private void removeAdView(ATBannerView ad) {
        if (ad != null) {
            ViewGroup parent = (ViewGroup) ad.getParent();
            if (parent != null) parent.removeView(ad);
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
