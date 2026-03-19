package com.cocos.game.adsetAdManager;

import android.app.Activity;
import android.graphics.Color;
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
import com.kc.openset.ad._native.OSETNative;
import com.kc.openset.ad._native.OSETNativeAd;
import com.kc.openset.ad.listener.OSETNativeAdLoadListener;
import com.kc.openset.ad.listener.OSETNativeListener;

import java.util.HashMap;
import java.util.Map;

// 信息流广告
public class AdFeed {

    private static AdFeed instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, Integer> lefts = new HashMap<>();
    private final Map<String, Integer> bottoms = new HashMap<>();
    private final Map<String, View> views = new HashMap<>();
    private final Map<String, OSETNativeAd> maps = new HashMap<>();

    public static AdFeed getInstance() {
        if (instance == null) {
            instance = new AdFeed();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int left, int bottom) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "信息流广告", id, "adset");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "信息流广告", id, "adset");
            return m_adMainCallBack;
        }

        OSETNativeAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "adset");
            loadFeedAd(id, left, bottom);
        } else {
            if (ad.isUsable()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "信息流广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "adset");
                loadFeedAd(id, left, bottom);
            }
        }
        return m_adMainCallBack;
    }

    private void loadFeedAd(String id, int left, int bottom) {
        removeAdView(id);
        maps.remove(id);
        OSETNative.getInstance()
                .setContext((Activity) m_mainInstance.getGameCtx())
                .setPosId(id)
                .loadAd(new OSETNativeAdLoadListener() {
                    @Override
                    public void onLoadSuccess(OSETNativeAd osetNativeAd) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "信息流广告", id, "adset", preload ? " 预加载" : "");
                        lefts.put(id, left);
                        bottoms.put(id, bottom);
                        maps.put(id, osetNativeAd);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String msg) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "信息流广告", id, "adset", preload ? " 预加载" : "", code, msg);
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            try {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, Integer.parseInt(code), msg);
                            } catch (NumberFormatException e) {
                                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, msg);
                            }
                        }
                    }
                });
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "信息流广告", id, "adset");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        OSETNativeAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isUsable()) {
            m_mainInstance.DebugPrintE("%s : %s %s (act == null || ad %s null || adView %s null || !ad.isUsable) ", "信息流广告", id, "adset", (ad == null) ? "==" : "!=");
            return;
        }

        OSETNativeAd finalAd = ad;
        int left = 0;
        if (lefts.containsKey(id)) {
            Integer t = lefts.get(id);
            if (t != null) left = t;
        }
        int bottom = 0;
        if (bottoms.containsKey(id)) {
            Integer b = bottoms.get(id);
            if (b != null) bottom = b;
        }
        int finalLeft = left;
        int finalBottom = bottom;
        ad.render((Activity) m_mainInstance.getGameCtx(), new OSETNativeListener() {
            @Override
            public void onClick(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "信息流广告", id, "adset");
            }

            @Override
            public void onClose(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "信息流广告", id, "adset");
                preload = true;
                loadFeedAd(id, finalLeft, finalBottom);
            }

            @Override
            public void onRenderSuccess(View adView) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "信息流广告", id, "adset");
                if (adView != null) {
                    views.put(id, adView);

                    FrameLayout container = m_mainInstance.getMainView();
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) {
                        parent.removeView(adView);
                        ViewGroup pParent = (ViewGroup) parent.getParent();
                        if (pParent != null) pParent.removeView(parent);
                    }

                    int match = FrameLayout.LayoutParams.MATCH_PARENT;
                    FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(match, match);
                    pParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    FrameLayout pParent = new FrameLayout(m_mainInstance.getGameCtx());
                    pParent.setId("feedAd".hashCode());
                    pParent.setBackgroundColor(Color.argb(50, 0, 0, 0));
                    container.addView(pParent, pParams);

                    adView.setBackgroundColor(Color.WHITE);
                    pParent.addView(adView, getLayoutParams(finalLeft, finalBottom));
                }
            }

            @Override
            public void onShow(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "信息流广告", id, "adset");

                // 广告ecpm，单位:千次分
                // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
                JSONObject obj = new JSONObject();
                String requestId = finalAd.getRequestId();
                String ecpm = String.valueOf(finalAd.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "feedAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onError(String code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染/曝光失败", "信息流广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    try {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, Integer.parseInt(code), msg);
                    } catch (NumberFormatException e) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, msg);
                    }
                }
            }
        });
    }

    private void removeAdView(String id) {
        OSETNativeAd ad = null;
        if (maps.containsKey(id)) ad = maps.get(id);
        if (ad != null) {
            View adView = null;
            if (views.containsKey(id)) adView = views.get(id);
            if (adView != null) {
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
                FrameLayout container = m_mainInstance.getMainView();
                View pView = container.findViewById("feedAd".hashCode());
                if (pView != null) container.removeView(pView);
                views.remove(id);
            }
            ad.destroy();
        }
    }

    private FrameLayout.LayoutParams getLayoutParams(int left, int bottom) {
        Point screenSize = m_mainInstance.getScreen();
        int width = screenSize.x - left * 2;
        int height = FrameLayout.LayoutParams.WRAP_CONTENT;
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
        params.bottomMargin = bottom;
        return params;
    }
}
