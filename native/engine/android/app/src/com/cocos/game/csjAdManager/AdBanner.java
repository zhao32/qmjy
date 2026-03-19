package com.cocos.game.csjAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTNativeExpressAd;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationAdEcpmInfo;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationBaseManager;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private TTAdNative adNativeLoader;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, AdSlot> slots = new HashMap<>();
    private final Map<String, TTNativeExpressAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "csj");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || id == null", "banner广告", id, "csj");
            return m_adMainCallBack;
        }

        TTNativeExpressAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "banner广告", id, "csj");
            loadBannerExpressAd(id, height, top);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "banner广告", id, "csj");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadBannerExpressAd(String id, int height, int top) {
        maps.remove(id);
        Point screenSize = m_mainInstance.getScreen();
        int minHeight = Math.round(screenSize.x / 6.4F);
        int finHeight = Math.max(minHeight, height);

        AdSlot adSlot = null;
        if (slots.containsKey(id)) {
            adSlot = slots.get(id);
        }
        if (adSlot == null) {
            adSlot = new AdSlot.Builder()
                    .setCodeId(id)  // 广告位ID
                    .setAdLoadType(TTAdLoadType.LOAD)
                    .setImageAcceptedSize(screenSize.x, finHeight) // 自渲染尺寸，单位 px
                    .setExpressViewAcceptedSize(m_mainInstance.px2dp(screenSize.x), m_mainInstance.px2dp(finHeight)) // 模板渲染尺寸，单位 dp
                    .supportRenderControl() // 支持模板样式
                    .setAdCount(1)
                    .build();
            slots.put(id, adSlot);
        }
        if (adNativeLoader == null)
            adNativeLoader = TTAdSdk.getAdManager().createAdNative(m_mainInstance.getGameCtx());
        adNativeLoader.loadBannerExpressAd(adSlot, new TTAdNative.NativeExpressAdListener() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                // 广告加载失败
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "banner广告", id, "csj", preload ? "预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onNativeExpressAdLoad(List<TTNativeExpressAd> list) {
                // 广告加载成功
                if (list != null && !list.isEmpty()) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "csj", preload ? "预加载" : "");
                    TTNativeExpressAd ttNativeExpressAd = list.get(0);
                    setExpressInteractionListener(ttNativeExpressAd, id, finHeight, top);
                    maps.put(id, ttNativeExpressAd);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                } else {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "banner广告", id, "csj", preload ? "预加载" : "");
                }
            }
        });
    }

    private void setExpressInteractionListener(TTNativeExpressAd ad, String id, int height, int top) {
        ad.setExpressInteractionListener(new TTNativeExpressAd.ExpressAdInteractionListener() {
            @Override
            public void onAdClicked(View view, int i) {
                // 广告点击
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "banner广告", id, "csj");
            }

            @Override
            public void onAdShow(View view, int i) {
                // 广告展示
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "banner广告", id, "csj");
                // 获取展示广告相关信息，需要再show回调之后进行获取
                MediationBaseManager manager = ad.getMediationManager();
                if (manager != null && manager.getShowEcpm() != null) {
                    MediationAdEcpmInfo showEcpm = manager.getShowEcpm();

                    JSONObject obj = new JSONObject();
                    String requestId = showEcpm.getRequestId();
                    String ecpm = showEcpm.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "bannerAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onRenderFail(View view, String s, int i) {
                // 广告渲染失败
                m_mainInstance.DebugPrintE("%s : %s %s 渲染失败 code:%s msg:%s", "banner广告", id, "csj", i, s);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, i, s);
                }
            }

            @Override
            public void onRenderSuccess(View view, float w, float h) {
                // 广告渲染成功
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "banner广告", id, "csj");
                View adView = ad.getExpressAdView();
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) parent.removeView(adView);
                    FrameLayout container = m_mainInstance.getMainView();
                    container.addView(adView, getLayoutParams(height, top));
                }
            }
        });

        // 用户点击不喜欢按钮回调
        ad.setDislikeCallback((Activity) m_mainInstance.getGameCtx(), new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {
                m_mainInstance.DebugPrintE("%s : %s %s onShow", "banner广告", id, "csj");
            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                // 用户点击了dislike按钮 点击了不喜欢按钮
                // 可能在这里写关闭逻辑
                m_mainInstance.DebugPrintE("%s : %s %s onSelected", "banner广告", id, "csj");
                removeAdView(ad);
                preload = true;
                loadBannerExpressAd(id, height, top);
            }

            @Override
            public void onCancel() {
                m_mainInstance.DebugPrintE("%s : %s %s onCancel", "banner广告", id, "csj");
            }
        });
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "banner广告", id, "csj");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        TTNativeExpressAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || container == null || ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView act == null || container == null || ad == null", "banner广告", id, "csj");
            return;
        }

        ad.render();
    }

    private void removeAdView(TTNativeExpressAd ad) {
        if (ad != null) {
            View adView = ad.getExpressAdView();
            if (adView != null) {
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
            }
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
