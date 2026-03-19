package com.umeng.sdk.wxapi;

import android.content.Intent;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson2.JSON;
import com.tencent.mm.opensdk.constants.ConstantsAPI;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.umeng.socialize.weixin.view.WXCallbackActivity;

public class WXEntryActivity extends WXCallbackActivity {
    @Override
    public void onResp(BaseResp resp) {
        if (resp.getType() == ConstantsAPI.COMMAND_OPEN_BUSINESS_VIEW) {
            WXOpenBusinessView.Resp launchResp = (WXOpenBusinessView.Resp) resp;
            Log.e("", String.format("BusinessViewResp: %s", JSON.toJSONString(launchResp)));

            Intent intent = new Intent("androidx.localbroadcastmanager");
            intent.putExtra("event", "requestMerchantTransfer");
            intent.putExtra("data", JSON.toJSONString(launchResp));
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }
        super.onResp(resp);
    }
}