package com.cocos.game.gdtAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.qq.e.ads.interstitial2.UnifiedInterstitialAD;
import com.qq.e.ads.interstitial2.UnifiedInterstitialADListener;
import com.qq.e.ads.interstitial2.UnifiedInterstitialMediaListener;
import com.qq.e.comm.pi.IBidding;
import com.qq.e.comm.util.AdError;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;
    private final Map<String, UnifiedInterstitialAD> maps = new HashMap<>();

    public static AdInterstitial getInstance() {
        if (instance == null) {
            instance = new AdInterstitial();
            instance.m_mainInstance = AdMain.getInstance();
        }
        return instance;
    }

    public AdMainCallBack LoadAd(String id) {
        preload = false;
        if (m_adMainCallBack == null) {
            m_adMainCallBack = new AdMainCallBack();
        }

        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "gdt");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "gdt");
            return m_adMainCallBack;
        }

        UnifiedInterstitialAD ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "gdt");
            loadInterstitialAD(id, activity);
        } else {
            if (ad.isValid()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "插屏广告", id, "gdt");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "gdt");
                ad.loadAD();
            }
        }

        return m_adMainCallBack;
    }

    private void loadInterstitialAD(String id, Activity activity) {
        maps.remove(id);
        UnifiedInterstitialAD ad = new UnifiedInterstitialAD(activity, id, new UnifiedInterstitialADListener() {
            @Override
            public void onADReceive() {
                // 广告加载完毕
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "gdt", preload ? " 预加载" : "");
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
                UnifiedInterstitialAD ad = maps.get(id);
                if (ad != null) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put(IBidding.EXPECT_COST_PRICE, ad.getECPM());
                    hashMap.put(IBidding.HIGHEST_LOSS_PRICE, Math.max(0, ad.getECPM() - 1));
                    ad.sendWinNotification(hashMap);
                }
            }

            @Override
            public void onVideoCached() {
                // 素材下载完成
            }

            @Override
            public void onNoAD(AdError adError) {
                // 广告加载或展示过程中出错
                m_mainInstance.DebugPrintE("%s : %s %s 加载或展示, code:%s, msg:%s", "插屏广告", id, "gdt", adError.getErrorCode(), adError.getErrorMsg());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, adError.getErrorCode(), adError.getErrorMsg());
                }
                maps.remove(id);
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
                UnifiedInterstitialAD ad = maps.get(id);
                if (ad != null) ad.destroy();
                preload = true;
                loadInterstitialAD(id, activity);
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
        maps.put(id, ad);
        ad.loadAD();
    }

    // 展示插全屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "gdt");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        UnifiedInterstitialAD ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isValid()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isValid", "插屏广告", id, "gdt");
            return;
        }

        ad.show(activity);

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
