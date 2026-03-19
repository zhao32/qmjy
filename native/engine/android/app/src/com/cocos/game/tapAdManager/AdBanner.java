package com.cocos.game.tapAdManager;

import android.app.Activity;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.tapsdk.tapad.AdRequest;
import com.tapsdk.tapad.TapAdManager;
import com.tapsdk.tapad.TapAdNative;
import com.tapsdk.tapad.TapBannerAd;

import java.util.HashMap;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, Integer> tops = new HashMap<>();
    private final Map<String, TapBannerAd> maps = new HashMap<>();

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, String uid, int top) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "tap");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || id == null", "banner广告", id, "tap");
            return m_adMainCallBack;
        }

        TapBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        tops.put(id, top);
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "tap");
            loadBannerExpressAd(id, uid);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "banner广告", id, "tap");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }
        return m_adMainCallBack;
    }

    private void loadBannerExpressAd(String id, String uid) {
        maps.remove(id);

        TapAdNative adNative = TapAdManager.get().createAdNative(m_mainInstance.getGameCtx());
        AdRequest adRequest = new AdRequest.Builder()
                .withSpaceId(Integer.parseInt(id))
                .withUserId(!TextUtils.isEmpty(uid) ? uid : "")
                .build();
        adNative.loadBannerAd(adRequest, new TapAdNative.BannerAdListener() {
            @Override
            public void onBannerAdLoad(TapBannerAd ad) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "tap", preload ? "预加载" : "");
                maps.put(id, ad);
                ad.setBannerInteractionListener(getBannerAdInteractionListener(id, uid, ad));
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "banner广告", id, "tap", preload ? "预加载" : "", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                }
            }
        });
    }

    private TapBannerAd.BannerInteractionListener getBannerAdInteractionListener(String id, String uid, TapBannerAd ad) {
        return new TapBannerAd.BannerInteractionListener() {
            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "banner广告", id, "tap");
            }

            @Override
            public void onAdValidShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示 Valid", "banner广告", id, "tap");
                JSONObject obj = new JSONObject();
                String requestId = "";
                double ecpm = 0;
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "banner广告", id, "tap");
                if (ad != null) {
                    removeAdView(ad.getBannerView());
                    ad.dispose();
                }
                preload = true;
                loadBannerExpressAd(id, uid);
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "banner广告", id, "tap");
            }

            @Override
            public void onDownloadClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击下载", "banner广告", id, "tap");
            }
        };
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "banner广告", id, "tap");
            return;
        }
        FrameLayout container = m_mainInstance.getMainView();
        TapBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (container == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView container == null || ad == null", "banner广告", id, "tap");
            return;
        }

        View adView = ad.getBannerView();
        removeAdView(adView);
        int top = 0;
        if (tops.containsKey(id)) {
            Integer t = tops.get(id);
            if (t != null) top = t;
        }
        if (adView != null) container.addView(adView, getLayoutParams(top));
    }

    private void removeAdView(View adView) {
        if (adView != null) {
            ViewGroup parent = (ViewGroup) adView.getParent();
            if (parent != null) parent.removeView(adView);
        }
    }

    private FrameLayout.LayoutParams getLayoutParams(int top) {
        int width = FrameLayout.LayoutParams.MATCH_PARENT;
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;
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
