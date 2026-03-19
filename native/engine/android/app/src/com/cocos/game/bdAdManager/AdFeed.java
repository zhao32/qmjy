package com.cocos.game.bdAdManager;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.baidu.mobads.sdk.api.BaiduNativeManager;
import com.baidu.mobads.sdk.api.ExpressResponse;
import com.baidu.mobads.sdk.api.RequestParameters;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 信息流广告
public class AdFeed {

    private static AdFeed instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, BaiduNativeManager> managers = new HashMap<>();
    private final Map<String, ExpressResponse> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "信息流广告", id, "bd");
            return m_adMainCallBack;
        }

        loadExpressAd(id, left, bottom);
        return m_adMainCallBack;
    }

    private void loadExpressAd(String id, int left, int bottom) {
        removeAdView(id);
        maps.remove(id);
        BaiduNativeManager manager = null;
        if (managers.containsKey(id)) {
            manager = managers.get(id);
        }
        if (manager == null) {
            manager = new BaiduNativeManager(m_mainInstance.getGameCtx(), id);
            managers.put(id, manager);
        }
        RequestParameters parameters = new RequestParameters.Builder().build();
        manager.loadExpressAd(parameters, new BaiduNativeManager.ExpressAdListener() {
            @Override
            public void onNativeLoad(List<ExpressResponse> list) {
                if (list != null && !list.isEmpty()) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "信息流广告", id, "bd", preload ? "预加载" : "");
                    ExpressResponse expressAd = list.get(0);
                    setInteractionListener(expressAd, id, left, bottom);
                    maps.put(id, expressAd);
                    if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.LOAD, id);
                    }
                } else {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "信息流广告", id, "bd", preload ? "预加载" : "");
                }
            }

            @Override
            public void onNativeFail(int errorCode, String errorMsg, ExpressResponse expressResponse) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 %s code:%s msg:%s", "信息流广告", id, "bd", preload ? " 预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onNoAd(int errorCode, String errorMsg, ExpressResponse expressResponse) {
                m_mainInstance.DebugPrintE("%s : %s %s 无广告返回 %s code:%s msg:%s", "信息流广告", id, "bd", preload ? " 预加载" : "", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }

            @Override
            public void onVideoDownloadSuccess() {
                m_mainInstance.DebugPrintE("%s : %s %s 素材缓存成功  %s", "信息流广告", id, "bd", preload ? " 预加载" : "");
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onVideoDownloadFailed() {
                m_mainInstance.DebugPrintE("%s : %s %s 素材缓存失败 %s", "信息流广告", id, "bd", preload ? " 预加载" : "");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, "");
                }
            }

            @Override
            public void onLpClosed() {
                m_mainInstance.DebugPrintE("%s : %s %s 落地页关闭", "信息流广告", id, "bd");
            }
        });
    }

    private void setInteractionListener(ExpressResponse ad, String id, int left, int bottom) {
        ad.setAdCloseListener(new ExpressResponse.ExpressCloseListener() {
            @Override
            public void onAdClose(ExpressResponse expressResponse) {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭", "信息流广告", id, "bd");
                preload = true;
                loadExpressAd(id, left, bottom);
            }
        });
        ad.setAdDislikeListener(new ExpressResponse.ExpressDislikeListener() {
            @Override
            public void onDislikeWindowShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 负反馈弹窗展示", "信息流广告", id, "bd");
            }

            @Override
            public void onDislikeItemClick(String reason) {
                m_mainInstance.DebugPrintE("%s : %s %s 负反馈选项点击 reason: %s", "信息流广告", id, "bd", reason);
                preload = true;
                loadExpressAd(id, left, bottom);
            }

            @Override
            public void onDislikeWindowClose() {
                m_mainInstance.DebugPrintE("%s : %s %s 负反馈弹窗关闭", "信息流广告", id, "bd");
            }
        });
        ad.setInteractionListener(new ExpressResponse.ExpressInteractionListener() {
            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 被点击", "信息流广告", id, "bd");
            }

            @Override
            public void onAdExposed() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光成功", "信息流广告", id, "bd");

                ExpressResponse ad = maps.get(id);
                if (ad != null) {
                    JSONObject obj = new JSONObject();
                    obj.put("adv_no", id);
                    obj.put("adv_ecpm", ad.getECPMLevel());
                    obj.put("adv_event", "feedAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdRenderFail(View view, String s, int i) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染失败", "信息流广告", id, "bd");
            }

            @Override
            public void onAdRenderSuccess(View view, float v, float v1) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "信息流广告", id, "bd");
                View adView = ad.getExpressAdView();
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) parent.removeView(adView);

                    int match = FrameLayout.LayoutParams.MATCH_PARENT;
                    FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(match, match);
                    pParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    FrameLayout pParent = new FrameLayout(m_mainInstance.getGameCtx());
                    pParent.setId("feedAd".hashCode());
                    pParent.setBackgroundColor(Color.argb(50, 0, 0, 0));
                    FrameLayout container = m_mainInstance.getMainView();
                    container.addView(pParent, pParams);

                    adView.setBackgroundColor(Color.WHITE);
                    pParent.addView(adView, getLayoutParams(left, bottom));
                }
            }

            @Override
            public void onAdUnionClick() {
                m_mainInstance.DebugPrintE("%s : %s %s 联盟官网点击", "信息流广告", id, "bd");
            }
        });
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "信息流广告", id, "bd");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        ExpressResponse ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady(activity)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isReady", "信息流广告", id, "bd");
            return;
        }
        ad.render();
    }

    private void removeAdView(String id) {
        ExpressResponse ad = null;
        if (maps.containsKey(id)) ad = maps.get(id);
        if (ad != null) {
            View adView = ad.getExpressAdView();
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
