package com.cocos.game.oppoAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.heytap.msp.mobad.api.ad.BannerAd;
import com.heytap.msp.mobad.api.listener.IBannerAdListener;

import java.util.HashMap;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private int height = 0, top = 0;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, BannerAd> maps = new HashMap<>();

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int height, int top) {
        preload = false;
        this.height = height;
        this.top = top;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "oppo");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || id == null", "banner广告", id, "oppo");
            return m_adMainCallBack;
        }

        BannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "oppo");
            loadBannerExpressAd(id);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "banner广告", id, "oppo");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }
        return m_adMainCallBack;
    }

    private void loadBannerExpressAd(String id) {
        maps.remove(id);
        BannerAd ad = new BannerAd((Activity) m_mainInstance.getGameCtx(), id);
        ad.setAdListener(new IBannerAdListener() {
            @Override
            public void onAdReady() {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "oppo", preload ? "预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onAdFailed(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s msg:%s", "banner广告", id, "oppo", preload ? "预加载" : "", s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }

            @Override
            public void onAdFailed(int i, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "banner广告", id, "oppo", preload ? "预加载" : "", i, s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, null);
                }
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "banner广告", id, "oppo");
                removeAdView(ad);
                preload = true;
                loadBannerExpressAd(id);
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "banner广告", id, "oppo");
                JSONObject obj = new JSONObject();
                String requestId = "";
                String ecpm = "";
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "banner广告", id, "oppo");
            }
        });
        ad.loadAd();
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "banner广告", id, "oppo");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        BannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || container == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView act == null || container == null || ad == null", "banner广告", id, "oppo");
            return;
        }

        View adView = ad.getAdView();
        removeAdView(ad);
        container.addView(adView, getLayoutParams(height, top));
    }

    private void removeAdView(BannerAd ad) {
        if (ad != null) {
            View adView = ad.getAdView();
            if (adView != null) {
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }
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
