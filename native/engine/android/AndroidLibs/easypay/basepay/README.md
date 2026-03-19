


    /**
     * 支付回调
     */
    private IPayCallback payCallback = new IPayCallback() {
        @Override
        public void success() {
            showPayResult("支付成功");
        }

        @Override
        public void failed(int code, String message) {
            showPayResult("支付失败");
        }

        @Override
        public void cancel() {
            showPayResult("支付取消");
        }
    };

    /**
     * 支付结果
     */
    protected void showPayResult(String msg) {
        runOnUiThread(() -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setTitle("支付结果通知");
            builder.setMessage(msg);
            builder.setNegativeButton("确定", (dialog, which) -> {
                        dialog.cancel();
                    }
            );
            builder.create().show();
        });
    }



    // 为微信支付设置 APPLICATION_ID
    android {
        manifestPlaceholders = [APPLICATION_ID: ""]
    }
    /**
     * 微信支付
     */
    protected void wxpay(JSONObject data, IPayCallback payCallback) {
        // 实例化微信支付策略
        WXPay wxPay = WXPay.getInstance();
        // 构造微信订单实体。一般都是由服务端直接返回。
        WXPayInfoImpli wxPayInfoImpli = new WXPayInfoImpli();
        wxPayInfoImpli.setAppid(getString(R.string.weixin_appid)); // app 保存，或者服务器保存
        wxPayInfoImpli.setPartnerid(data.optString("partnerid", ""));
        wxPayInfoImpli.setPrepayId(data.optString("prepayid", ""));
        wxPayInfoImpli.setPackageValue(data.optString("package", ""));
        wxPayInfoImpli.setNonceStr(data.optString("noncestr", ""));
        wxPayInfoImpli.setTimestamp(data.optString("timestamp", ""));
        wxPayInfoImpli.setSign(data.optString("sign", ""));
        // 策略场景类调起支付方法开始支付，以及接收回调。
        EasyPay.pay(wxPay, this, wxPayInfoImpli, payCallback);
    }

    /**
     * 支付宝支付
     */
    protected void alipay(String orderInfo, IPayCallback payCallback) {
        // 实例化支付宝支付策略
        AliPay aliPay = new AliPay();
        // 构造支付宝订单实体。一般都是由服务端直接返回。
        AlipayInfoImpli alipayInfoImpli = new AlipayInfoImpli();
        alipayInfoImpli.setOrderInfo(orderInfo);
        // 策略场景类调起支付方法开始支付，以及接收回调。
        EasyPay.pay(aliPay, this, alipayInfoImpli, payCallback);
    }

    /**
     * 银联支付《云闪付》
     */
    protected void unionpay(JSONObject data, IPayCallback payCallback) {
        // 实例化银联支付策略
        UnionPay unionPay = new UnionPay();
        // 构造银联订单实体。一般都是由服务端直接返回。测试时可以用Mode.TEST,发布时用Mode.RELEASE。
        UnionPayInfoImpli unionPayInfoImpli = new UnionPayInfoImpli();
        unionPayInfoImpli.setTn(data.optString("tn", ""));
        unionPayInfoImpli.setMode(BuildConfig.DEBUG ? Mode.TEST : Mode.RELEASE);
        // 策略场景类调起支付方法开始支付，以及接收回调。
        EasyPay.pay(unionPay, this, unionPayInfoImpli, payCallback);
    }