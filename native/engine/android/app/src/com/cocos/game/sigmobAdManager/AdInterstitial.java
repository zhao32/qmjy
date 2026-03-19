package com.cocos.game.sigmobAdManager;

import android.app.Activity;
import android.text.TextUtils;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.cocos.game.JsbBridgeCallback;
import com.cocos.game.adManager.AdManager;
import com.sigmob.windad.WindAdError;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAd;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdListener;
import com.sigmob.windad.newInterstitial.WindNewInterstitialAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardAdRequest;
import com.sigmob.windad.rewardVideo.WindRewardVideoAd;

import java.util.HashMap;
import java.util.Map;

// 插屏广告
public class AdInterstitial {

    private static AdInterstitial instance;
    private AdMain m_mainInstance;
    private boolean preload = false;
    private AdMainCallBack m_adMainCallBack;

    private final Map<String, Object> options = new HashMap<>();
    private final Map<String, WindNewInterstitialAdRequest> requests = new HashMap<>();
    private final Map<String, WindNewInterstitialAd> maps = new HashMap<>();

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
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已经展示", "插屏广告", id, "sigmob");
            return m_adMainCallBack;
        }

        Activity activity = (Activity) m_mainInstance.getGameCtx();
        if (activity == null || TextUtils.isEmpty(id)) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || id == null", "插屏广告", id, "sigmob");
            return m_adMainCallBack;
        }

        WindNewInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (ad == null) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "sigmob");
            loadInterstitialAd(id);
        } else {
            if (ad.isReady()) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 已预加载", "插屏广告", id, "sigmob");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            } else {
                m_mainInstance.DebugPrintE("%s : %s %s 开始加载 未预加载", "插屏广告", id, "sigmob");
                ad.loadAd();
            }
        }

        return m_adMainCallBack;
    }

    private void loadInterstitialAd(String id) {
        maps.remove(id);
        WindNewInterstitialAdRequest request = null;
        if (requests.containsKey(id)) {
            request = requests.get(id);
        }
        if (request == null) {
            // placementId 必填；user_id、options可不填，
            request = new WindNewInterstitialAdRequest(id, null, options);
            requests.put(id, request);
        }
        WindNewInterstitialAd ad = new WindNewInterstitialAd(request);
        ad.setWindNewInterstitialAdListener(new WindNewInterstitialAdListener() {
            @Override
            public void onInterstitialAdLoadSuccess(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 加载成功 %s", "插屏广告", id, "sigmob", preload ? " 预加载" : "");
                maps.put(id, ad);
                if (!preload && m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onSuccess(AdMainCallBack.LoadStatusType.RENDER, id);
                }
            }

            @Override
            public void onInterstitialAdLoadError(WindAdError windAdError, String s) {
                // 广告加载或展示过程中出错
                m_mainInstance.DebugPrintE("%s : %s %s 加载错误 %s, code:%s, msg:%s", "插屏广告", id, "sigmob", preload ? " 预加载" : "", windAdError.getErrorCode(), windAdError.getMessage());
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.LOAD, null, windAdError.getErrorCode(), windAdError.getMessage());
                }
            }

            @Override
            public void onInterstitialAdPreLoadSuccess(String s) {
            }

            @Override
            public void onInterstitialAdPreLoadFail(String s) {
            }

            @Override
            public void onInterstitialAdShow(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "插屏广告", id, "sigmob");
                WindNewInterstitialAd ad = maps.get(id);
                JSONObject obj = new JSONObject();
                obj.put("adv_no", id);
                if (ad != null) {
                    String ecpm = ad.getEcpm();
                    obj.put("adv_ecpm", ecpm);
                }
                obj.put("adv_event", "interstitialAd");
                JsbBridgeCallback.getInstance().sendToScript("showAd", JSON.toJSONString(obj));
            }

            @Override
            public void onInterstitialAdShowError(WindAdError windAdError, String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 展示失败", "插屏广告", id, "sigmob");
                if (m_adMainCallBack.adLoadStatusCallBack != null) {
                    m_adMainCallBack.adLoadStatusCallBack.onError(AdMainCallBack.LoadStatusType.RENDER, null, windAdError.getErrorCode(), windAdError.getMessage());
                }
            }

            @Override
            public void onInterstitialAdClicked(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 被用户点击", "插屏广告", id, "sigmob");
            }

            @Override
            public void onInterstitialAdClosed(String s) {
                m_mainInstance.DebugPrintE("%s : %s %s 广告关闭", "插屏广告", id, "sigmob");
                preload = true;
                loadInterstitialAd(id);
            }
        });
        ad.loadAd();
    }

    // 展示插全屏广告
    public void ShowAd(String id) {
        if (AdManager.getInstance().getInterstitialState() == 1) {
            m_mainInstance.DebugPrintE("%s : %s %s 开始展示 已经展示", "插屏广告", id, "sigmob");
            return;
        }
        Activity activity = (Activity) m_mainInstance.getGameCtx();
        WindNewInterstitialAd ad = null;
        if (maps.containsKey(id)) {
            ad = maps.get(id);
        }
        if (activity == null || ad == null || !ad.isReady()) {
            m_mainInstance.DebugPrintE("%s : %s %s act == null || ad == null || !isReady", "插屏广告", id, "sigmob");
            return;
        }
        m_mainInstance.DebugPrintE("%s : %s %s 开始展示", "插屏广告", id, "sigmob");
        HashMap<String, String> showOption = new HashMap<>();
        ad.show(showOption);
    }

}
