package com.cocos.game.gdtAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.pi.IBidding;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;

// 插屏广告
public class AdInterstitialFull {

    private static AdInterstitialFull instance;
    private AdMain m_mainInstance;
    private UnifiedInterstitialAD ad;
    private AdMainCallBack m_adMainCallBack;

    public static AdInterstitialFull getInstance() {
        if (instance == null) {
            instance = new AdInterstitialFull();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "gdt");
            return m_adMainCallBack;
        }

        if (ad == null) {
            // 广告加载、渲染、点击状态的回调
            ad = new UnifiedInterstitialAD(activity, id, new UnifiedInterstitialADListener() {
                @Override
                public void onADReceive() {
                    // 广告加载完毕
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                    }
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(IBidding.EXPECT_COST_PRICE, ad.getECPM());
                    hashMap.put(IBidding.HIGHEST_LOSS_PRICE, Math.max(0, ad.getECPM() - 1));
                    ad.sendWinNotification(hashMap);
                }

                @Override
                public void onVideoCached() {
                    // 素材下载完成
                }

                @Override
                public void onNoAD(AdError adError) {
                    // 广告加载或展示过程中出错
                    m_mainInstance.DebugPrintE("%s : %s %s 加载错误, code:%s, msg:%s", "插屏广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, adError.getErrorCode(), adError.getErrorMsg());
                    }
                }

                @Override
                public void onADOpened() {
                    // 广告展开
                }

                @Override
                public void onADExposure() {
                    // 广告曝光
                }

                @Override
                public void onADClicked() {
                    // 广告点击
                }

                @Override
                public void onADLeftApplication() {
                    // 由于广告点击离开 APP
                }

                @Override
                public void onADClosed() {
                    // 广告关闭
                }

                @Override
                public void onRenderSuccess() {
                    // 渲染成功
                }

                @Override
                public void onRenderFail() {
                    // 渲染失败
                }
            });

            // 视频广告播放状态回调，专用于带有视频素材的广告对象
            ad.setMediaListener(new UnifiedInterstitialMediaListener() {
                @Override
                public void onVideoInit() {
                    // View 初始化完成
                }

                @Override
                public void onVideoLoading() {
                    // 下载中
                }

                @Override
                public void onVideoReady(long duration) {
                    // 播放器初始化完成
                }

                @Override
                public void onVideoStart() {
                    // 开始播放
                }

                @Override
                public void onVideoPause() {
                    // 暂停播放
                }

                @Override
                public void onVideoComplete() {
                    // 播放结束
                }

                @Override
                public void onVideoError(AdError adError) {
                    // 播放时出现错误
                    m_mainInstance.DebugPrintE("%s : %s %s 视频播放错误, code:%s, msg:%s", "插屏广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                    if (m_adMainCallBack.adLoadStatusCallBack != null) {
                        m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, adError.getErrorCode(), adError.getErrorMsg());
                    }
                }

                @Override
                public void onVideoPageOpen() {
                }

                @Override
                public void onVideoPageClose() {
                }
            });
        }

        ad.loadFullScreenAD();

        return m_adMainCallBack;
    }

    // 展示插全屏广告
    public void ShowAd(String id) {
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || ad == null || !ad.isValid()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isValid", "插屏广告", id, "gdt");
            return;
        }

        ad.showFullScreenAD(activity);

        JSONObject obj = new JSONObject();
        if (ad.getExtraInfo() != null) {
            Object rid = ad.getExtraInfo().get("request_id");
            String requestId = rid != null ? JSON.toJSONString(rid) : id;
            obj.put("adv_no", !TextUtils.isEmpty(requestId) ? requestId : id);
            obj.put("adv_ecpm", ad.getECPM());
        }
        obj.put("adv_event", "interstitialAd");
        JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
    }

}
