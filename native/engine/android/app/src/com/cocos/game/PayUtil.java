package com.cocos.game;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.text.TextUtils;
import android.util.Log;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.alibaba.fastjson2.JSON;
import com.chrishui.alipay.AliPay;
import com.chrishui.alipay.AlipayInfoImpli;
import com.chrishui.easypay.EasyPay;
import com.chrishui.easypay.callback.IPayCallback;
import com.chrishui.wxpay.WXPay;
import com.chrishui.wxpay.WXPayInfoImpli;
import com.cocos.service.SDKWrapper;
import com.tencent.mm.opensdk.constants.Build;
import com.tencent.mm.opensdk.modelbiz.WXOpenBusinessView;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class PayUtil {
    private PayUtil() {
    }

    private static final IPayCallback payCallback = new IPayCallback() {
        @Override
        public void success() {
            JsbBridgeCallback.getInstance().sendToScript("payment", "1");
        }

        @Override
        public void failed(int code, String message) {
            JsbBridgeCallback.getInstance().sendToScript("payment", "");
        }

        @Override
        public void cancel() {
            JsbBridgeCallback.getInstance().sendToScript("payment", "");
        }
    };

    public static void wxpay(String data) {
        try {
            JSONObject jsonData = new JSONObject(data);
            // 实例化微信支付策略
            WXPay wxPay = WXPay.getInstance();
            // 构造微信订单实体。一般都是由服务端直接返回。
            WXPayInfoImpli wxPayInfoImpli = new WXPayInfoImpli();
            wxPayInfoImpli.setAppid(jsonData.getString("appid"));
            wxPayInfoImpli.setPartnerid(jsonData.getString("partnerid"));
            wxPayInfoImpli.setPrepayId(jsonData.getString("prepayid"));
            wxPayInfoImpli.setPackageValue(jsonData.getString("package"));
            wxPayInfoImpli.setNonceStr(jsonData.getString("noncestr"));
            wxPayInfoImpli.setTimestamp(jsonData.getString("timestamp"));
            wxPayInfoImpli.setSign(jsonData.getString("sign"));
            // 策略场景类调起支付方法开始支付，以及接收回调。
            EasyPay.pay(wxPay, SDKWrapper.shared().getActivity(), wxPayInfoImpli, payCallback);
        } catch (JSONException e) {
            e.printStackTrace();
            JsbBridgeCallback.getInstance().sendToScript("payment", "");
        }
    }

    public static void alipay(String data) {
        SDKWrapper.shared().getActivity().runOnUiThread(() -> {
            // 实例化支付宝支付策略
            AliPay aliPay = AliPay.getInstance();
            // 构造支付宝订单实体。一般都是由服务端直接返回。
            AlipayInfoImpli alipayInfoImpli = new AlipayInfoImpli();
            alipayInfoImpli.setOrderInfo(data);
            // 策略场景类调起支付方法开始支付，以及接收回调。
            EasyPay.pay(aliPay, SDKWrapper.shared().getActivity(), alipayInfoImpli, payCallback);
        });
    }

    public static void requestMerchantTransfer(Context context, String mchId, String appId, String pkg) {
        try {
            register(context);
            IWXAPI api = WXAPIFactory.createWXAPI(context, appId, true);
            api.registerApp(appId);
            if (api.isWXAppInstalled() && api.getWXAppSupportAPI() >= Build.OPEN_BUSINESS_VIEW_SDK_INT) {
                WXOpenBusinessView.Req req = new WXOpenBusinessView.Req();
                req.businessType = "requestMerchantTransfer";
                req.query = String.format("mchId=%s&appId=%s&package=%s",
                        URLEncoder.encode(mchId, "UTF-8"),
                        URLEncoder.encode(appId, "UTF-8"),
                        URLEncoder.encode(pkg, "UTF-8"));
                api.sendReq(req);
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static LocalBroadcastReceiver receiver;
    private static final IntentFilter filter = new IntentFilter("androidx.localbroadcastmanager");

    public static void register(Context context) {
        if (receiver == null) {
            receiver = new LocalBroadcastReceiver();
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter);
        }
    }

    public static void unregister(Context context) {
        LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver);
        receiver = null;
    }

    public static class LocalBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (TextUtils.equals("androidx.localbroadcastmanager", intent.getAction())) {
                String event = intent.getStringExtra("event");
                if (TextUtils.equals("requestMerchantTransfer", event)) {
                    String data = intent.getStringExtra("data");
                    Log.e("", String.format("BusinessBroadcast: %s", data));
                    // {"businessType":"requestMerchantTransfer","errCode":0,"extMsg":"{\"result\":\"success\"}","openId":"ojcPO61yiWbTFVMDgtsGqj0FzeoM","type":26}
                    // {"businessType":"requestMerchantTransfer","errCode":-2,"extMsg":"{\"result\":\"cancel\"}","openId":"ojcPO61yiWbTFVMDgtsGqj0FzeoM","type":26}
                    // {"businessType":"requestMerchantTransfer","errCode":-3,"extMsg":"{\"result\":\"fail\"}","openId":"ojcPO61yiWbTFVMDgtsGqj0FzeoM","type":26}
                    com.alibaba.fastjson2.JSONObject resp = JSON.parseObject(data, com.alibaba.fastjson2.JSONObject.class);
                    JsbBridgeCallback.getInstance().sendToScript("requestMerchantTransfer", String.format("{\"code\":\"%s\"}",
                            resp != null ? resp.getString("errCode") : "-3"));
                }
            }
        }
    }
}
