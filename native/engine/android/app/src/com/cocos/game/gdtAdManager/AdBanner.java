package com.cocos.game.gdtAdManager;

import android.app.Activity;
import android.graphics.Point;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.qq.e.ads.banner2.UnifiedBannerADListener;
import com.qq.e.ads.banner2.UnifiedBannerView;
import com.qq.e.comm.pi.IBidding;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, Integer> heights = new HashMap<>();
    private final Map<String, Integer> tops = new HashMap<>();
    private final Map<String, UnifiedBannerView> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "banner广告", id, "gdt");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "banner广告", id, "gdt");
            return m_adMainCallBack;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始加载", "banner广告", id, "gdt");
        UnifiedBannerView ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            loadBannerView(id, height, top, activity);
        } else {
            if (ad.isValid()) {
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                ad.loadAD();
            }
        }
        return m_adMainCallBack;
    }

    private void loadBannerView(String id, int height, int top, Activity activity) {
        maps.remove(id);
        UnifiedBannerView ad = new UnifiedBannerView(activity, id, new UnifiedBannerADListener() {
            @Override
            public void onNoAD(AdError adError) {
                // 广告加载或展示过程中出错
                m_mainInstance.DebugPrintE("%s : %s %s 加载或展示 code:%s, msg:%s", "banner广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, adError.getErrorCode(), adError.getErrorMsg());
                }
                maps.remove(id);
            }

            @Override
            public void onADReceive() {
                // 广告加载成功
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "banner广告", id, "gdt", preload ? " 预加载" : "");
                heights.put(id, height);
                tops.put(id, top);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
                UnifiedBannerView ad = maps.get(id);
                if (ad != null) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(IBidding.EXPECT_COST_PRICE, ad.getECPM());
                    hashMap.put(IBidding.HIGHEST_LOSS_PRICE, Math.max(0, ad.getECPM() - 1));
                    ad.sendWinNotification(hashMap);
                }
            }

            @Override
            public void onADExposure() {
                // 广告曝光
            }

            @Override
            public void onADClosed() {
                // 广告关闭
                UnifiedBannerView ad = maps.get(id);
                if (ad != null) {
                    ViewGroup parent = (ViewGroup) ad.getParent();
                    if (parent != null) {
                        parent.removeView(ad);
                        ViewGroup pParent = (ViewGroup) parent.getParent();
                        if (pParent != null) pParent.removeView(parent);
                    }
                    ad.destroy();
                }
                preload = true;
                loadBannerView(id, height, top, activity);
            }

            @Override
            public void onADClicked() {
                // 广告点击
            }

            @Override
            public void onADLeftApplication() {
                // 由于广告点击离开 APP
            }
        });
        maps.put(id, ad);
        ad.loadAD();
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getBannerState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "banner广告", id, "gdt");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        UnifiedBannerView ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isValid()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isValid", "banner广告", id, "gdt");
            return;
        }

        ViewGroup parent = (ViewGroup) ad.getParent();
        if (parent != null) {
            parent.removeView(ad);
            ViewGroup pParent = (ViewGroup) parent.getParent();
            if (pParent != null) pParent.removeView(parent);
        }
        FrameLayout container = m_mainInstance.getMainView();
        Optional<Integer> height = Optional.ofNullable(heights.get(id));
        Optional<Integer> top = Optional.ofNullable(tops.get(id));
        container.addView(ad, getLayoutParams(height.isPresent() ? height.get() : 0, top.isPresent() ? top.get() : 0));

        JSONObject obj = new JSONObject();
        if (ad.getExtraInfo() != null) {
            Object rid = ad.getExtraInfo().get("request_id");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ad.getECPM());
        }
        obj.put("adv_event", "bannerAd");
        JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
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
