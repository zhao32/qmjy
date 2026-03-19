package com.cocos.game.csjAdManager;

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
import com.baidu.mobads.sdk.api.RequestParameters;
import com.bytedance.sdk.openadsdk.AdSlot;
import com.bytedance.sdk.openadsdk.TTAdDislike;
import com.bytedance.sdk.openadsdk.TTAdLoadType;
import com.bytedance.sdk.openadsdk.TTAdNative;
import com.bytedance.sdk.openadsdk.TTAdSdk;
import com.bytedance.sdk.openadsdk.TTFeedAd;
import com.bytedance.sdk.openadsdk.mediation.MediationConstant;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationAdSlot;
import com.bytedance.sdk.openadsdk.mediation.ad.MediationExpressRenderListener;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationAdEcpmInfo;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationBaseManager;
import com.bytedance.sdk.openadsdk.mediation.manager.MediationNativeManager;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.qq.e.ads.cfg.DownAPPConfirmPolicy;
import com.qq.e.ads.cfg.VideoOption;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 信息流广告
public class AdFeed {
    private static AdFeed instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private TTAdNative adNativeLoader;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, AdSlot> slots = new HashMap<>();
    private final Map<String, TTFeedAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "信息流广告", id, "csj");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        if (activity == null || container == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || container == null || id == null", "信息流广告", id, "csj");
            return m_adMainCallBack;
        }

        TTFeedAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "csj");
            loadFeedAd(container, id, left, bottom);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已预加载", "信息流广告", id, "csj");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadFeedAd(FrameLayout container, String id, int left, int bottom) {
        removeAdView(id);
        maps.remove(id);
        AdSlot adSlot = null;
        if (slots.containsKey(id)) {
            adSlot = slots.get(id);
        }
        if (adSlot == null) {
            Point screenSize = m_mainInstance.getScreen();
            int width = screenSize.x - left * 2;
            int height = FrameLayout.LayoutParams.WRAP_CONTENT;
            adSlot = new AdSlot.Builder()
                    .setCodeId(id)
                    .setAdLoadType(TTAdLoadType.LOAD)
                    .setImageAcceptedSize(width, height) // 自渲染尺寸，单位 px
                    .setExpressViewAcceptedSize(m_mainInstance.px2dp(width), height) // 模板渲染尺寸，单位 dp
                    .supportRenderControl() // 支持模板样式
                    .setAdCount(1)
                    .setMediationAdSlot(new MediationAdSlot.Builder()
                            .setExtraObject(MediationConstant.KEY_GDT_VIDEO_OPTION,
                                    new VideoOption.Builder()
                                            .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                                            .setAutoPlayMuted(true)
                                            .build())
                            .setExtraObject(MediationConstant.KEY_GDT_MIN_VIDEO_DURATION, 1000)
                            .setExtraObject(MediationConstant.KEY_GDT_MAX_VIDEO_DURATION, 2000)
                            .setExtraObject(MediationConstant.KEY_GDT_DOWN_APP_CONFIG_POLICY, DownAPPConfirmPolicy.NOConfirm)
                            .setExtraObject(MediationConstant.KEY_BAIDU_REQUEST_PARAMETERS,
                                    new RequestParameters.Builder()
                                            .downloadAppConfirmPolicy(RequestParameters.DOWNLOAD_APP_CONFIRM_ALWAYS)
                                            .build())
                            .setExtraObject(MediationConstant.KEY_BAIDU_CACHE_VIDEO_ONLY_WIFI, true)
                            .build())
                    .build();
            slots.put(id, adSlot);
        }

        if (adNativeLoader == null)
            adNativeLoader = TTAdSdk.getAdManager().createAdNative(m_mainInstance.getGameCtx());
        adNativeLoader.loadFeedAd(adSlot, new TTAdNative.FeedAdListener() {
            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "信息流广告", id, "csj", preload ? "预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onFeedAdLoad(List<TTFeedAd> list) {
                if (list != null && !list.isEmpty()) {
                    TTFeedAd ttFeedAd = list.get(0);
                    // 用户点击不喜欢按钮回调
                    ttFeedAd.setDislikeCallback((Activity) m_mainInstance.getGameCtx(), getDislikeCallback(container, id, left, bottom));
                    MediationNativeManager manager = ttFeedAd.getMediationManager();
                    if (manager != null) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s %s",
                                "信息流广告", id, "csj", preload ? "预加载" : "", manager.isExpress() ? "模板" : "自渲染");
                        if (manager.isExpress()) {
                            // 模板信息流广告
                            // 模板广告展示监听器
                            ttFeedAd.setExpressRenderListener(getExpressAdInteractionListener(container, ttFeedAd, id, left, bottom));
                        } else {
                            // 自渲染信息流广告
                            // 自渲染广告展示监听器
                        }
                    }
                    maps.put(id, ttFeedAd);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                } else {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "信息流广告", id, "csj", preload ? "预加载" : "");
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                    }
                }
            }
        });
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "信息流广告", id, "csj");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        FrameLayout container = m_mainInstance.getMainView();
        TTFeedAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null || activity == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s ad == null || act == null || container == null", "信息流广告", id, "csj");
            return;
        }

        MediationNativeManager manager = ad.getMediationManager();
        if (manager != null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 %s", "信息流广告", id, "csj", manager.isExpress() ? "模板" : "自渲染");
            if (manager.isExpress()) {
                // 模板信息流广告
                // 调用 render 方法进行渲染，在 onRenderSuccess 中展示广告
                ad.render();
            } else {
                // 自渲染信息流广告
            }
        }
    }

    private TTAdDislike.DislikeInteractionCallback getDislikeCallback(FrameLayout container, String id, int left, int bottom) {
        return new TTAdDislike.DislikeInteractionCallback() {
            @Override
            public void onShow() {
                m_mainInstance.DebugPrintE("%s : %s %s TTAdDislike onShow", "信息流广告", id, "csj");
            }

            @Override
            public void onSelected(int position, String value, boolean enforce) {
                // 用户点击 dislike 后回调
                m_mainInstance.DebugPrintE("%s : %s %s TTAdDislike onSelected: %s | %s | %s", "信息流广告", id, "csj", position, value, enforce);
                preload = true;
                loadFeedAd(container, id, left, bottom);
            }

            @Override
            public void onCancel() {
                m_mainInstance.DebugPrintE("%s : %s %s TTAdDislike onCancel", "信息流广告", id, "csj");
            }
        };
    }

    private MediationExpressRenderListener getExpressAdInteractionListener(FrameLayout container, TTFeedAd ad, String id, int left, int bottom) {
        return new MediationExpressRenderListener() {
            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击", "信息流广告", id, "csj");
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 广告展示", "信息流广告", id, "csj");
                // 获取展示广告相关信息，需要再 show 回调之后进行获取
                MediationBaseManager manager = ad.getMediationManager();
                if (manager != null && manager.getShowEcpm() != null) {
                    MediationAdEcpmInfo showEcpm = manager.getShowEcpm();

                    JSONObject obj = new JSONObject();
                    String requestId = showEcpm.getRequestId();
                    String ecpm = showEcpm.getEcpm();
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "feedAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onRenderFail(View view, String msg, int code) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染失败 code:%s, msg:%s", "信息流广告", id, "csj", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, msg);
                }
            }

            @Override
            public void onRenderSuccess(View view, float w, float h, boolean isExpress) {
                // *** 注意不要使用 onRenderSuccess 参数中的 view ***
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "信息流广告", id, "csj");

                View adView = ad.getAdView();
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) parent.removeView(adView);

                    int match = FrameLayout.LayoutParams.MATCH_PARENT;
                    FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(match, match);
                    pParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    FrameLayout pParent = new FrameLayout(m_mainInstance.getGameCtx());
                    pParent.setId("feedAd".hashCode());
                    pParent.setBackgroundColor(Color.argb(50, 0, 0, 0));
                    container.addView(pParent, pParams);

                    adView.setBackgroundColor(Color.WHITE);
                    pParent.addView(adView, getLayoutParams(left, bottom));
                }
            }
        };
    }

    private void removeAdView(String id) {
        TTFeedAd ad = null;
        if (maps.containsKey(id)) ad = maps.get(id);
        if (ad != null) {
            View adView = ad.getAdView();
            if (adView != null) {
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
                FrameLayout container = m_mainInstance.getMainView();
                View pView = container.findViewById("feedAd".hashCode());
                if (pView != null) container.removeView(pView);
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
