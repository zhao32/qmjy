package com.cocos.game.adsetAdManager;

import android.app.Activity;
import android.graphics.Color;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.kc.openset.ad.banner.OSETBanner;
import com.kc.openset.ad.banner.OSETBannerAd;
import com.kc.openset.ad.listener.OSETBannerAdLoadListener;
import com.kc.openset.ad.listener.OSETBannerListener;

import java.util.HashMap;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, Integer> tops = new HashMap<>();
    private final Map<String, OSETBannerAd> maps = new HashMap<>();

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int top) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "adset");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = AdMain.getInstance().getMainView();
        if (activity == null || TextUtils.isEmpty(id) || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null || container == null", "banner广告", id, "adset");
            return m_adMainCallBack;
        }

        OSETBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        tops.put(id, top);
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "adset");
            loadBannerAd(id, top);
        } else {
            if (ad.isUsable()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "banner广告", id, "adset");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "adset");
                loadBannerAd(id, top);
            }
        }
        return m_adMainCallBack;
    }

    private void loadBannerAd(String id, int top) {
        maps.remove(id);
        OSETBanner.getInstance()
                .setContext((Activity) m_mainInstance.getGameCtx())
                .setPosId(id)
                .loadAd(new OSETBannerAdLoadListener() {
                    @Override
                    public void onLoadSuccess(OSETBannerAd osetBannerAd) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "adset", preload ? " 预加载" : "");
                        maps.put(id, osetBannerAd);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    }

                    @Override
                    public void onLoadFail(String code, String msg) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "banner广告", id, "adset", preload ? " 预加载" : "", code, msg);
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
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = AdMain.getInstance().getMainView();
        OSETBannerAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isUsable() || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s (act == null || ad %s null || !ad.isUsable || container %s null) ", "banner广告", id, "adset", (ad == null) ? "==" : "!=", (container == null) ? "==" : "!=");
            return;
        }

        int top = 0;
        if (tops.containsKey(id)) {
            Integer t = tops.get(id);
            if (t != null) top = t;
        }
        OSETBannerAd finalAd = ad;
        int finalTop = top;
        ad.render((Activity) m_mainInstance.getGameCtx(), new OSETBannerListener() {
            @Override
            public void onClick(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "banner广告", id, "adset");
            }

            @Override
            public void onClose(View adView) {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "banner广告", id, "adset");
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) parent.removeView(adView);
                }
                finalAd.destroy();
                preload = true;
                loadBannerAd(id, finalTop);
            }

            @Override
            public void onRenderSuccess(View adView) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "banner广告", id, "adset");
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) parent.removeView(adView);
                    adView.setBackgroundColor(Color.argb(50, 0, 0, 0));
                    container.addView(adView, getLayoutParams(finalTop));
                }
            }

            @Override
            public void onShow(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "banner广告", id, "adset");

                // 广告ecpm，单位:千次分
                // 注意，只有自营奇点广告返回价格，聚合广告AdSet不返回价格
                JSONObject obj = new JSONObject();
                String requestId = finalAd.getRequestId();
                String ecpm = String.valueOf(finalAd.getECPM());
                obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                obj.put("adv_ecpm", ecpm);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onError(String code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染/曝光失败", "banner广告", id, "adset");
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
