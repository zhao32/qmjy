package com.cocos.game.ksAdManager;

import android.app.Activity;
import android.graphics.Color;
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
import com.kwad.sdk.api.KsDrawAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// draw信息流广告
public class AdDraw {
    private static AdDraw instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, KsScene> scenes = new HashMap<>();
    private final Map<String, KsDrawAd> maps = new HashMap<>();

    public static AdDraw getInstance() {
        if (instance == null) {
            instance = new AdDraw();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, int bottom) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getDrawState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "draw信息流广告", id, "ks");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "draw信息流广告", id, "ks");
            return m_adMainCallBack;
        }

        KsDrawAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "draw信息流广告", id, "ks");
            loadDrawAd(id, bottom);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "draw信息流广告", id, "ks");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadDrawAd(String id, int bottom) {
        removeAdView(id);
        maps.remove(id);
        if (KsAdSDK.getLoadManager() != null) {

            KsScene scene = null;
            if (scenes.containsKey(id)) {
                scene = scenes.get(id);
            }
            if (scene == null) {
                Point screenSize = m_mainInstance.getScreen();
                int width = screenSize.x;
                int height = (int) (screenSize.y * (bottom > 0 ? 0.8 : 0.9));
                scene = new KsScene.Builder(Long.parseLong(id))
                        .width(width) // 如果这里要传具体数字 像素单位要传 px 格式
                        .height(height)
                        .adNum(1)
                        .build();
                scenes.put(id, scene);
            }
            KsAdSDK.getLoadManager().loadDrawAd(scene, new KsLoadManager.DrawAdListener() {
                @Override
                public void onError(int code, String msg) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误 code:%s, msg:%s", "draw信息流广告", id, "ks", code, msg);
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                    }
                }

                @Override
                public void onDrawAdLoad(@Nullable List<KsDrawAd> list) {
                    if (list != null && !list.isEmpty()) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "draw信息流广告", id, "ks", preload ? "预加载" : "");
                        KsDrawAd ksFeedAd = list.get(0);
                        setAdInteractionListener(ksFeedAd, id, bottom);
                        maps.put(id, ksFeedAd);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    } else {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "draw信息流广告", id, "ks", preload ? "预加载" : "");
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                        }
                    }
                }
            });
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "draw信息流广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }
    }

    private void setAdInteractionListener(KsDrawAd ad, String id, int bottom) {
        View adView = ad.getDrawView(m_mainInstance.getGameCtx());
        if (adView != null) adView.setLayoutParams(getLayoutParams(bottom));
        ad.setAdInteractionListener(new KsDrawAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被点击", "draw信息流广告", id, "ks");
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光", "draw信息流广告", id, "ks");
            }

            @Override
            public void onVideoPlayStart() {
                m_mainInstance.DebugPrintE("%s : %s %s 开始播放", "draw信息流广告", id, "ks");
            }

            @Override
            public void onVideoPlayPause() {
                m_mainInstance.DebugPrintE("%s : %s %s 暂停播放", "draw信息流广告", id, "ks");
            }

            @Override
            public void onVideoPlayResume() {
                m_mainInstance.DebugPrintE("%s : %s %s 恢复播放", "draw信息流广告", id, "ks");
            }

            @Override
            public void onVideoPlayEnd() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放结束", "draw信息流广告", id, "ks");
            }

            @Override
            public void onVideoPlayError() {
                m_mainInstance.DebugPrintE("%s : %s %s 播放异常", "draw信息流广告", id, "ks");
            }
        });
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getDrawState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "draw信息流广告", id, "ks");
            return;
        }
        KsDrawAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView ad == null || container == null", "draw信息流广告", id, "ks");
            return;
        }

        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "draw信息流广告", id, "ks");
        View adView = ad.getDrawView(m_mainInstance.getGameCtx());
        if (adView != null) {
            int match = FrameLayout.LayoutParams.MATCH_PARENT;
            FrameLayout.LayoutParams pParams = new FrameLayout.LayoutParams(match, match);
            pParams.gravity = Gravity.CENTER;
            FrameLayout pParent = new FrameLayout(m_mainInstance.getGameCtx());
            pParent.setId("drawAd".hashCode());
            pParent.setOnClickListener(v -> {
                preload = true;
                loadDrawAd(id, 0);
            });
            pParent.setBackgroundColor(Color.argb(50, 0, 0, 0));
            pParent.addView(adView);
            container.addView(pParent, pParams);

            JSONObject obj = new JSONObject();
            Map<String, Object> extra = ad.getMediaExtraInfo();
            Object rid = extra.get("transId");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            String ecpm = String.valueOf(ad.getECPM());
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ecpm);
            obj.put("adv_event", "drawAd");
            JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
        }
    }

    private void removeAdView(String id) {
        KsDrawAd ad = null;
        if (maps.containsKey(id)) ad = maps.get(id);
        if (ad != null) {
            View adView = ad.getDrawView(m_mainInstance.getGameCtx());
            if (adView != null) {
                ViewGroup parent = (ViewGroup) adView.getParent();
                if (parent != null) parent.removeView(adView);
                FrameLayout container = m_mainInstance.getMainView();
                View pView = container.findViewById("drawAd".hashCode());
                if (pView != null) container.removeView(pView);
            }
        }
    }

    private FrameLayout.LayoutParams getLayoutParams(int bottom) {
        Point screenSize = m_mainInstance.getScreen();
        int width = screenSize.x;
        int height = (int) (screenSize.y * (bottom > 0 ? 0.8 : 0.9));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(width, height);
        params.gravity = bottom > 0 ? Gravity.CENTER : (Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
        return params;
    }
}
