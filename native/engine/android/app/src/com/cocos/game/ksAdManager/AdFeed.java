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
import com.kwad.sdk.api.KsFeedAd;
import com.kwad.sdk.api.KsLoadManager;
import com.kwad.sdk.api.KsScene;
import com.qq.e.ads.rewardvideo.RewardVideoAD;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// 信息流广告
public class AdFeed {
    private static AdFeed instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, KsScene> scenes = new HashMap<>();
    private final Map<String, KsFeedAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "信息流广告", id, "ks");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "信息流广告", id, "ks");
            return m_adMainCallBack;
        }

        KsFeedAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "信息流广告", id, "ks");
            loadConfigFeedAd(id, left, bottom);
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "信息流广告", id, "ks");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
            }
        }

        return m_adMainCallBack;
    }

    private void loadConfigFeedAd(String id, int left, int bottom) {
        maps.remove(id);
        if (KsAdSDK.getLoadManager() != null) {
            Point screenSize = m_mainInstance.getScreen();
            int width = screenSize.x - left * 2;

            KsScene scene = null;
            if (scenes.containsKey(id)) {
                scene = scenes.get(id);
            }
            if (scene == null) {
                scene = new KsScene.Builder(Long.parseLong(id))
                        .width(width) // 如果这里要传具体数字 像素单位要传 px 格式
                        .adNum(1)
                        .build();
                scenes.put(id, scene);
            }
            KsAdSDK.getLoadManager().loadConfigFeedAd(scene, new KsLoadManager.FeedAdListener() {
                @Override
                public void onError(int code, String msg) {
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误 code:%s, msg:%s", "信息流广告", id, "ks", code, msg);
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, code, msg);
                    }
                }

                @Override
                public void onFeedAdLoad(@Nullable List<KsFeedAd> list) {
                    if (list != null && !list.isEmpty()) {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "信息流广告", id, "ks", preload ? "预加载" : "");
                        KsFeedAd ksFeedAd = list.get(0);
                        View adView = ksFeedAd.getFeedView(m_mainInstance.getGameCtx());
                        if (adView != null) {
                            adView.setLayoutParams(getLayoutParams(left, bottom));
                        }
                        setAdInteractionListener(ksFeedAd, id, left, bottom);
                        maps.put(id, ksFeedAd);
                        if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                        }
                    } else {
                        m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s 但是列表中没有内容", "信息流广告", id, "ks", preload ? "预加载" : "");
                        if (m_adMainCallBack.adLoadStatusCallBack != null) {
                            m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "没有匹配的广告");
                        }
                    }
                }
            });
        } else {
            m_mainInstance.DebugPrintE("%s : %s %s 加载错误, msg:%s", "信息流广告", id, "ks", "未初始化");
            if (m_adMainCallBack.adLoadStatusCallBack != null) {
                m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, 0, "未初始化");
            }
        }
    }

    public void ShowAd(String id) {
        if (AdManager.getInstance().getFeedState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "信息流广告", id, "ks");
            return;
        }
        KsFeedAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        FrameLayout container = m_mainInstance.getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s %s showBannerView ad == null || container == null", "信息流广告", id, "ks");
            return;
        }

        KsAdVideoPlayConfig videoPlayConfig = new KsAdVideoPlayConfig.Builder()
                .videoAutoPlayType(KsAdVideoPlayConfig.VideoAutoPlayType.AUTO_PLAY)
                .build();
        ad.setVideoPlayConfig(videoPlayConfig);
        ad.render(new KsFeedAd.AdRenderListener() {
            @Override
            public void onAdRenderSuccess(View view) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染成功", "信息流广告", id, "ks");
                KsFeedAd ad = null;
                if (maps.containsKey(id)) {
                    ad = maps.get(id);
                }
                if (ad != null) {
                    View adView = ad.getFeedView(m_mainInstance.getGameCtx());
                    if (adView != null) {
                        ViewGroup parent = (ViewGroup) adView.getParent();
                        if (parent != null) {
                            parent.removeView(adView);
                            ViewGroup pParent = (ViewGroup) parent.getParent();
                            if (pParent != null) pParent.removeView(parent);
                        }
                        container.addView(adView);
                    }

                    JSONObject obj = new JSONObject();
                    Map<String, Object> extra = ad.getMediaExtraInfo();
                    Object rid = extra.get("transId");
                    String requestId = rid != null ? JSON.toJSONString(rid) : id;
                    String ecpm = String.valueOf(ad.getECPM());
                    obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
                    obj.put("adv_ecpm", ecpm);
                    obj.put("adv_event", "feedAd");
                    JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
                }
            }

            @Override
            public void onAdRenderFailed(int code, String msg) {
                m_mainInstance.DebugPrintE("%s : %s %s 渲染失败 code:%s, msg:%s", "信息流广告", id, "ks", code, msg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, code, msg);
                }
            }
        });
    }

    private void setAdInteractionListener(KsFeedAd ad, String id, int left, int bottom) {
        ad.setAdInteractionListener(new KsFeedAd.AdInteractionListener() {
            @Override
            public void onAdClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "信息流广告", id, "ks");
            }

            @Override
            public void onAdShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 曝光", "信息流广告", id, "ks");
            }

            @Override
            public void onDislikeClicked() {
                m_mainInstance.DebugPrintE("%s : %s %s 点击不喜欢", "信息流广告", id, "ks");
                View adView = ad.getFeedView(m_mainInstance.getGameCtx());
                if (adView != null) {
                    ViewGroup parent = (ViewGroup) adView.getParent();
                    if (parent != null) {
                        parent.removeView(adView);
                        ViewGroup pParent = (ViewGroup) parent.getParent();
                        if (pParent != null) pParent.removeView(parent);
                    }
                }
                preload = true;
                loadConfigFeedAd(id, left, bottom);
            }

            @Override
            public void onDownloadTipsDialogShow() {
                m_mainInstance.DebugPrintE("%s : %s %s 展示下载合规弹窗", "信息流广告", id, "ks");
            }

            @Override
            public void onDownloadTipsDialogDismiss() {
                m_mainInstance.DebugPrintE("%s : %s %s 关闭下载合规弹窗", "信息流广告", id, "ks");
            }
        });
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
