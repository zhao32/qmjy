package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kwad.sdk.api.KsAdSDK;
import com.kwad.sdk.api.KsAdVideoPlayConfig;
import com.kwad.sdk.api.KsBannerAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.kwad.sdk.api.SdkConfig;

import java.util.HashMap;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, KsScene> scenes = new HashMap<>();
    private final Map<String, View> views = new HashMap<>();
    private final Map<String, KsBannerAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "横幅广告", id, "ks");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "横幅广告", id, "ks");
            return m_adMainCallBack;
        }

        KsBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "横幅广告", id, "ks");
            loadBannerAd(id, height, top);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "横幅广告", id, "ks");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadBannerAd(String id, int height, int top) {
        removeAdView(id);
        maps.remove(id);
        if (KsAdSDK.getLoadManager() != null) {

            KsScene scene = null;
            if (scenes.containsKey(id)) {
                scene = scenes.get(id);
            }
            if (scene == null) {
                Point screenSize = m_mainInstance.getScreen();
                int minHeight = Math.round(screenSize.x / 6.4F);
                int finHeight = Math.max(minHeight, height);
                scene = new KsScene.Builder(Long.parseLong(id))
                        .width(screenSize.x) // 如果这里要传具体数字 像素单位要传 px 格式
                        .height(finHeight)
                        .screenOrientation(SdkConfig.SCREEN_ORIENTATION_PORTRAIT)
                        .build();
                scenes.put(id, scene);
            }
            KsAdSDK.getLoadManager().loadBannerAd(scene, new KsLoadManager.BannerAdListener() {
                @Override
                public void onError(int code, String msg) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误 code:%s, msg:%s", "横幅广告", id, "ks", code, msg);
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                    }
                }

                @Override
                public void onBannerAdLoad(@Nullable KsBannerAd ksBannerAd) {
                    if (ksBannerAd == null) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载完成 %s", "横幅广告", id, "ks", "没有内容");
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有内容");
                        }
                        return;
                    }
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "横幅广告", id, "ks", preload ? "预加载" : "");
                    setAdInteractionListener(ksBannerAd, id, height, top);
                    maps.put(id, ksBannerAd);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                }
            });
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "横幅广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }
    }

    private void setAdInteractionListener(KsBannerAd ad, String id, int height, int top) {
        View adView = ad.getView(m_mainInstance.getGameCtx(), new KsBannerAd.BannerAdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被点击", "横幅广告", id, "ks");
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光", "横幅广告", id, "ks");
                JSONObject obj = new JSONObject();
                Map<String, Object> extra = ad.getMediaExtraInfo();
                Object rid = extra.get("transId");
                String requestId = rid != null ? JSON.toJSONString(rid) : id;
                String ecpm = String.valueOf(ad.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "横幅广告", id, "ks");
                removeAdView(id);
                maps.remove(id);
                preload = true;
                loadBannerAd(id, height, top);
            }

            @Override
            public void onAdShowError(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示异常 code:%s, msg:%s", "横幅广告", id, "ks", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, msg);
                }
            }
        }, new KsAdVideoPlayConfig.Builder().build());
        adView.setLayoutParams(getLayoutParams(height, top));
        views.put(id, adView);
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "横幅广告", id, "ks");
            return;
        }
        KsBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        View adView = null;
        if (views.containsKey(id)) {
            adView = views.get(id);
        }
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || adView == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView ad == null || adView == null || container == null", "横幅广告", id, "ks");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "横幅广告", id, "ks");
        ViewGroup parent = (ViewGroup) adView.getParent();
        if (parent != null) parent.removeView(adView);
        container.addView(adView);
    }

    private void removeAdView(String id) {
        View adView = null;
        if (views.containsKey(id)) adView = views.get(id);
        if (adView != null) {
            ViewGroup parent = (ViewGroup) adView.getParent();
            if (parent != null) {
                parent.removeView(adView);
            }
        }
        views.remove(id);
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
