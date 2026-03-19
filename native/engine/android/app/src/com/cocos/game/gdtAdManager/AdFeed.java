package com.cocos.game.gdtAdManager;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.qq.e.ads.cfg.VideoOption;
import com.qq.e.ads.nativ.ADSize;
import com.qq.e.ads.nativ.NativeExpressAD;
import com.qq.e.ads.nativ.NativeExpressADView;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 信息流广告
public class AdFeed {
    private static AdFeed instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, NativeExpressAD> maps = new HashMap<>();
    private final Map<String, NativeExpressADView> views = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "信息流广告", id, "gdt");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "信息流广告", id, "gdt");
            return m_adMainCallBack;
        }

        NativeExpressADView adView = null;
        if (views.containsKey(id)) {
            adView = views.get(id);
        }
        if (adView == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "gdt");
            loadNativeAD(id, left, bottom, activity);
        } else {
            if (adView.isValid()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "信息流广告", id, "gdt");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "gdt");
                loadNativeAD(id, left, bottom, activity);
            }
        }

        return m_adMainCallBack;
    }

    private void loadNativeAD(String id, int left, int bottom, Activity activity) {
        NativeExpressAD ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            Point screenSize = m_mainInstance.getScreen();
            int width = screenSize.x - left * 2;
            ADSize adSize = new ADSize(width, ADSize.AUTO_HEIGHT);
            ad = new NativeExpressAD(activity, adSize, id, new NativeExpressAD.NativeExpressADListener() {
                @Override
                public void onADLoaded(List<NativeExpressADView> list) {
                    if (list != null && !list.isEmpty()) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "信息流广告", id, "gdt", preload ? " 预加载" : "");
                        NativeExpressADView adView = list.get(0);
                        FrameLayout.LayoutParams params = getLayoutParams(left, bottom);
                        adView.setLayoutParams(params);
                        views.put(id, adView);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    } else {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "信息流广告", id, "gdt", preload ? " 预加载" : "");
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                        }
                    }
                }

                @Override
                public void onRenderFail(NativeExpressADView nativeExpressADView) {
                }

                @Override
                public void onRenderSuccess(NativeExpressADView nativeExpressADView) {
                }

                @Override
                public void onADExposure(NativeExpressADView nativeExpressADView) {
                }

                @Override
                public void onADClicked(NativeExpressADView nativeExpressADView) {
                }

                @Override
                public void onADClosed(NativeExpressADView nativeExpressADView) {
                    NativeExpressADView adView = views.get(id);
                    if (adView != null) {
                        ViewGroup parent = (ViewGroup) adView.getParent();
                        if (parent != null) {
                            parent.removeView(adView);
                            ViewGroup pParent = (ViewGroup) parent.getParent();
                            if (pParent != null) pParent.removeView(parent);
                        }
                        adView.destroy();
                    }
                    views.remove(id);
                    preload = true;
                    loadNativeAD(id, left, bottom, activity);
                }

                @Override
                public void onADLeftApplication(NativeExpressADView nativeExpressADView) {
                }

                @Override
                public void onNoAD(AdError adError) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载或展示, code:%s, msg:%s", "信息流广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, adError.getErrorCode(), adError.getErrorMsg());
                    }
                    maps.remove(id);
                }
            });
            ad.setVideoOption(new VideoOption.Builder()
                    .setAutoPlayPolicy(VideoOption.AutoPlayPolicy.ALWAYS)
                    .setAutoPlayMuted(true)
                    .build());
            maps.put(id, ad);
        }
        ad.loadAD(1);
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "信息流广告", id, "gdt");
            return;
        }
        FrameLayout container = m_mainInstance.getMainView();
        NativeExpressADView adView = null;
        if (views.containsKey(id)) {
            adView = views.get(id);
        }
        if (adView == null || !adView.isValid() || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showFeedView adView == null || !adView.isValid() || container == null", "信息流广告", id, "gdt");
            return;
        }

        adView.render();
        adView.setBackgroundColor(Color.WHITE);
        ViewGroup parent = (ViewGroup) adView.getParent();
        if (parent != null) {
            parent.removeView(adView);
            ViewGroup pParent = (ViewGroup) parent.getParent();
            if (pParent != null) pParent.removeView(parent);
        }
        container.addView(adView);

        JSONObject obj = new JSONObject();
        if (adView.getExtraInfo() != null) {
            Object rid = adView.getExtraInfo().get("request_id");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", adView.getECPM());
        }
        obj.put("adv_event", "feedAd");
        JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
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
