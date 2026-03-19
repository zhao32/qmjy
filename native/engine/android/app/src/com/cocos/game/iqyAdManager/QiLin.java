package com.cocos.game.iqyAdManager;

import android.content.Context;
import android.text.TextUtils;

import com.iqiyi.qilin.trans.QiLinTrans;
import com.iqiyi.qilin.trans.TransParam;

import org.json.JSONObject;

public class QiLin {
    private static QiLin instance;
    private boolean m_isInit;

    private QiLin() {
    }

    public static QiLin getInstance() {
        if (instance == null) {
            instance = new QiLin();
        }
        return instance;
    }

    public void init(Context context, String appId, String channelId, String oaid) {
        if (m_isInit) {
            return;
        }
        if (TextUtils.isEmpty(appId)) {
            return;
        }
        m_isInit = true;
        QiLinTrans.setDebug(TransParam.LogLevel.LOG_DEBUG, false, "");
        QiLinTrans.init(context, appId, channelId, oaid, null);
    }

    public void uploadTrans(String transType, JSONObject transParam) {
        QiLinTrans.uploadTrans(transType, transParam);
    }

    public void onResume() {
        QiLinTrans.onResume();
    }

    public void onDestroy() {
        QiLinTrans.onDestroy();
    }
}
