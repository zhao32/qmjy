
# 友盟文档
# https://developer.umeng.com/docs/128606/detail/193879


# UMeng 混淆 Start
-keepattributes EnclosingMethod,Exceptions

-dontwarn com.google.android.maps.**
-dontwarn com.squareup.okhttp3.**
-keep public class javax.**
-dontwarn android.webkit.WebView
-keep public class android.webkit.**
-dontwarn android.support.v4.**
-keepnames class * implements android.os.Parcelable {
    public static final ** CREATOR;
}

-keep class com.uyumao.** { *; }
-keep public interface com.umeng.socialize.**
-keep public interface com.umeng.socialize.sensor.**
-keep public interface com.umeng.scrshot.**
-keep public class com.umeng.socialize.* { *; }
-keep class com.umeng.** { *; }
-keep class com.umeng.**
-dontwarn com.umeng.**
-keep public class com.umeng.soexample.R$*{
    public static final int *;
}

-keep class com.facebook.**
-keep class com.facebook.** { *; }
-keep public interface com.facebook.**
-keep enum com.facebook.**
-dontwarn com.facebook.**

-keep class im.yixin.sdk.api.YXMessage { *; }
-keep class im.yixin.sdk.api.** implements im.yixin.sdk.api.YXMessage$YXMessageData{ *; }

-dontwarn twitter4j.**
-keep class twitter4j.** { *; }

-dontwarn com.tencent.weibo.sdk.**
-dontwarn com.tencent.**
-keep public interface com.tencent.**
-keep public class com.tencent.** { *; }
-keep class com.tencent.** { *; }

-keep class com.kakao.** { *; }
-dontwarn com.kakao.**

-keep class com.sina.** { *; }
-dontwarn com.sina.**

-keep class  com.alipay.share.sdk.** { *; }

-keep class com.linkedin.** { *; }
-keep public class com.linkedin.android.mobilesdk.R$*{
    public static final int *;
}

-keep class com.android.dingtalk.share.ddsharemodule.** { *; }
# UMeng 混淆 End



#支付宝混淆 START
-keep class com.alipay.android.app.IAlixPay { *; }
-keep class com.alipay.android.app.IAlixPay$Stub { *; }
-keep class com.alipay.android.app.IRemoteServiceCallback { *; }
-keep class com.alipay.android.app.IRemoteServiceCallback$Stub { *; }
-keep class com.alipay.sdk.app.PayTask { public *; }
-keep class com.alipay.sdk.app.AuthTask { public *; }
-keep class com.alipay.sdk.app.H5PayCallback {
    <fields>;
    <methods>;
}
-keep class com.alipay.android.phone.mrpc.core.** { *; }
-keep class com.alipay.apmobilesecuritysdk.** { *; }
-keep class com.alipay.mobile.framework.service.annotation.** { *; }
-keep class com.alipay.mobilesecuritysdk.face.** { *; }
-keep class com.alipay.tscenter.biz.rpc.** { *; }
-keep class com.alipay.tscenter.** { *; }
-keep class org.json.alipay.** { *; }
-keep class com.ta.utdid2.** { *; }
-keep class com.ut.device.** { *; }
#支付宝混淆 END



-keep class com.umeng.soexample.** { *; }
-keep class com.uc.** {*;}
-keep class com.efs.** {*;}
-keepclassmembers class *{
    public<init>(org.json.JSONObject);
}
-keepclassmembers enum *{
    public static **[] values();
    public static ** valueOf(java.lang.String);
}