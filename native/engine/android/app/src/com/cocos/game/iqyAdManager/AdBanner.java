package com.cocos.game.iqyAdManager;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.mcto.sspsdk.IQYNative;
import com.mcto.sspsdk.IQyBanner;
import com.mcto.sspsdk.QyAdSlot;
import com.mcto.sspsdk.QyBannerStyle;
import com.mcto.sspsdk.QySdk;

import java.util.Map;

// 横幅广告
public class AdBanner {
    private static AdBanner instance;
    private AdMain m_mainInstance;
    private AdMainCallBack m_adMainCallBack;
    private IQyBanner ad;
    private int top = 0;

    public static AdBanner getInstance() {
        if (instance == null) {
            instance = new AdBanner();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id, long channel, int top) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        this.top = top;

        IQYNative iQyNative = QySdk.getAdClient().createAdNative(m_mainInstance.getGameCtx());
        // 设置广告位属性
        QyAdSlot slot = QyAdSlot.newQyBannerAdSlot()
                // 广告位ID
                .codeId(id)
                // 频道id，接入方自定义的值
                .channelId(channel)
                // Banner广告样式，{@link#com.mcto.sspsdk.QyBannerStyle}
                .bannerStyle(QyBannerStyle.QYBANNER_STRIP)
                .build();

        iQyNative.loadBannerAd(slot, new IQYNative.BannerAdListener() {
            @Override
            public void onBannerAdLoad(IQyBanner iQyBanner) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功", "banner广告", id, "iqy");
                ad = iQyBanner;
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onError(int errorCode, String errorMsg) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载失败 code:%s msg:%s", "banner广告", id, "iqy", errorCode, errorMsg);
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, errorCode, errorMsg);
                }
            }
        });
        return m_adMainCallBack;
    }

    // 展示Banner广告
    public void ShowAd(String id) {
        FrameLayout container = AdMain.getInstance().getMainView();
        if (ad == null || container == null) {
            m_mainInstance.DebugPrintE("%s : %s showBannerView ad == null || container == null", "banner广告", "iqy");
            return;
        }

        ad.setBannerInteractionListener(new IQyBanner.IAdInteractionListener() {
            @Override
            public void onAdShow() {
                Map<String, String> adExtra = ad.getAdExtra();
                m_mainInstance.DebugPrintE("%s : %s 播放开始, adExtra=[%s]", "banner广告", "iqy", JSON.toJSONString(adExtra));

                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                obj.put("adv_event", "bannerAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onAdClick() {
                m_mainInstance.DebugPrintE("%s : %s 播放点击", "banner广告", "iqy");
            }

            @Override
            public void onAdClose() {
                m_mainInstance.DebugPrintE("%s : %s 播放关闭", "banner广告", "iqy");
            }

            @Override
            public void onRenderSuccess() {
                m_mainInstance.DebugPrintE("%s : %s 渲染成功", "banner广告", "iqy");
            }

            @Override
            public void onAdStart() {
                m_mainInstance.DebugPrintE("%s : %s 播放开始", "banner广告", "iqy");
            }

            @Override
            public void onAdStop() {
                m_mainInstance.DebugPrintE("%s : %s 播放停止", "banner广告", "iqy");
            }

            @Override
            public void onAdComplete() {
                m_mainInstance.DebugPrintE("%s : %s 播放完成", "banner广告", "iqy");
            }

            @Override
            public void onAdPlayError() {
                m_mainInstance.DebugPrintE("%s : %s 播放失败", "banner广告", "iqy");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, 0, null);
                }
            }
        });

        // 建议在注册监听之后添加，防止收不到广告展示和点击事件回调
        View adView = ad.getBannerView();
        ViewGroup parent = (ViewGroup) adView.getParent();
        if (parent != null) parent.removeView(adView);
        container.addView(adView, getLayoutParams(top));
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
