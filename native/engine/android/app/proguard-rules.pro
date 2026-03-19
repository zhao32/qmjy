# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in E:\developSoftware\Android\SDK/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

-dontoptimize

# androidx
-keep class com.google.android.material.** { *; }
-keep class androidx.** { *; }
-keep public class * extends androidx.**
-keep interface androidx.** { *; }
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

# android.support.v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

# Proguard Cocos2d-x-lite for release
-keep public class com.cocos.** { *; }
-dontwarn com.cocos.**
-keep public class org.cocos2dx.** { *; }
-dontwarn org.cocos2dx.**

# Proguard Apache HTTP for release
-keep class org.apache.http.** { *; }
-dontwarn org.apache.http.**

# Proguard okhttp for release
-keep class okhttp3.** { *; }
-dontwarn okhttp3.**
-keep class okio.** { *; }
-dontwarn okio.**

# Proguard Android Webivew for release. you can comment if you are not using a webview
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient
-keep public class com.google.** { *; }
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient

# This is generated automatically by the Android Gradle plugin.
-dontwarn android.hardware.BatteryState
-dontwarn android.hardware.lights.Light
-dontwarn android.hardware.lights.LightState$Builder
-dontwarn android.hardware.lights.LightState
-dontwarn android.hardware.lights.LightsManager$LightsSession
-dontwarn android.hardware.lights.LightsManager
-dontwarn android.hardware.lights.LightsRequest$Builder
-dontwarn android.hardware.lights.LightsRequest
-dontwarn android.net.ssl.SSLSockets
-dontwarn android.os.VibratorManager

# Proguard Glide for release
-keep class com.bumptech.glide.** { *; }
-dontwarn com.bumptech.glide.**
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# Proguard alipay for release
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
-keep class com.ta.utdid2.** { *; }
-keep class com.ut.device.** { *; }
-keep class org.json.alipay.** { *; }

# Proguard tencent.mm sdk for release
-keep class com.tencent.mm.opensdk.** { *; }
-dontwarn com.tencent.mm.opensdk.**

# Proguard tencent.qq sdk for release
-keep class com.tencent.connect.** { *; }
-dontwarn com.tencent.connect.**
-keep class com.tencent.open.** { *; }
-dontwarn com.tencent.open.**
-keep class com.tencent.tauth.** { *; }
-dontwarn com.tencent.tauth.**

# Proguard sina sdk for release
-keep class com.sina.weibo.sdk.** { *; }
-dontwarn com.sina.weibo.sdk.**

# Proguard fastjson2 for release
-keep class com.alibaba.fastjson2.** { *; }
-dontwarn com.alibaba.fastjson2.**

# Proguard bytedance for release
-keep class com.bykv.** { *; }
-dontwarn com.bykv.**
-keep class com.byted.** { *; }
-dontwarn com.byted.**
-keep class com.bytedance.** { *; }
-dontwarn com.bytedance.**
-keep class com.ss.** { *; }
-dontwarn com.ss.**

# Proguard volcengine for release
-keepattributes Signature
-keepattributes Exceptions,InnerClasses
-keep class ms.bz.bd.** { *; }
-keep class com.volcengine.mobsecBiz.** { *; }
-keep class com.bytedance.frameworks.encryptor.EncryptorUtil { *; }

# mediation
-keep class bykvm*.**
-keep class com.bytedance.msdk.adapter.** { public *; }
-keep class com.bytedance.msdk.api.** {
 public *;
}

# ks 不接入 ks sdk 可以不引入
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.** { *; }
-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**

# baidu 不接入 baidu sdk 可以不引入
-ignorewarnings
-dontwarn com.baidu.mobads.sdk.api.**
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.baidu.mobads.** { *; }
-keep class com.style.widget.** { *; }
-keep class com.component.** { *; }
-keep class com.baidu.ad.magic.flute.** { *; }
-keep class com.baidu.mobstat.forbes.** { *; }

# sigmob 不接入 sigmob sdk 可以不引入
-dontwarn com.sigmob.**
-keep class com.sigmob.**.** { *; }
-keep interface com.sigmob.**.**{ *; }
-keep class com.czhj.**{ *; }
-keep interface com.czhj.**{ *; }
-keep class sun.misc.Unsafe { *; }
# miitmdid
-dontwarn com.bun.**
-keep class com.bun.** { *; }
-keep class a.** { *; }
-keep class XI.CA.XI.** { *; }
-keep class XI.K0.XI.** { *; }
-keep class XI.XI.K0.** { *; }
-keep class XI.vs.K0.** { *; }
-keep class XI.xo.XI.XI.** { *; }
-keep class com.asus.msa.SupplementaryDID.** { *; }
-keep class com.asus.msa.sdid.** { *; }
-keep class com.huawei.hms.ads.identifier.** { *; }
-keep class com.samsung.android.deviceidservice.** { *; }
-keep class com.zui.opendeviceidlibrary.** { *; }
-keep class org.json.** { *; }
-keep public class com.netease.nis.sdkwrapper.Utils {
    public <methods>;
}

# Proguard 4399 for release
-keep class cn.m4399.operate.** { *; }
-dontwarn cn.m4399.operate.**

# Proguard hykb for release
-keep class com.m3839.sdk.common.** { *; }
-keep class com.m3839.sdk.anti.** { *; }
-keep class com.m3839.sdk.user.** { *; }
-keep class com.m3839.sdk.login.** { *; }
-keep class com.m3839.sdk.single.** { *; }
-keepclasseswithmembernames class com.m3839.sdk.common.js.JsInterface {
    <methods>;
}

# Proguard taptap for release
-keep class com.taptap.sdk.** { *; }
-dontwarn com.taptap.sdk.**

# 该代码混淆文件为 YSDK 对外提供个游戏开发者集成 YSDK 时使用
-optimizationpasses 5                   # 指定代码的压缩级别
-dontusemixedcaseclassnames             # 指定代码的压缩级别
-dontskipnonpubliclibraryclasses        # 是否混淆第三方jar
-dontpreverify                          # 混淆时是否做预校验
-dontoptimize
-ignorewarnings                          # 忽略警告，避免打包时某些警告出现
-verbose                                # 混淆时是否记录日志
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*    # 混淆时所采用的算法

-dontwarn com.tencent.**
-keep class android.support.** {*;}
-keep class com.tencent.** {*;}
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.preference.Preference
-keep public class com.android.vending.licensing.ILicensingService
-keep class * extends android.app.Dialog {*;}
-keepclasseswithmembernames class * {
    native <methods>;
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepattributes InnerClasses
-keep class android.os.**{*;}
-keep class org.apache.http.** { * ;}
-keep class com.qq.jce.**{*;}
-keep class com.qq.taf.**{*;}
-keep class common.**{*;}
-keep class exceptionupload.**{*;}
-keep class mqq.**{*;}
-keep class qimei.**{*;}
-keep class strategy.**{*;}
-keep class userinfo.**{*;}
-keep class com.pay.**{*;}
-keep class com.demon.plugin.**{*;}
-keep class oicq.wlogin_sdk.**{*;}
-keep class com.alipay.sdk.**{*;}
-keep class com.migu.sdk.**{*;}
-keep class com.alipay.sdk.*
-keep class com.migu.sdk.*
-keep class com.qq.taf.jce.**{*;}

-dontwarn java.nio.file.Files
-dontwarn java.nio.file.Path
-dontwarn java.nio.file.OpenOption
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement

-keep class com.alipay.sdk.app.AuthTask {*;}
-keep class com.alipay.sdk.app.PayTask {*;}

-dontwarn com.tencent.**
-keep class com.qq.** {*;}
# 设备指纹SDK
-keep public class qfc.**{*;}
-keep class com.bun.miitmdid.** {*;}
# 该代码混淆文件为 YSDK 对外提供个游戏开发者集成 YSDK 时使用

# Proguard alibaba oss for release
-keep class com.alibaba.sdk.android.oss.** { *; }
-dontwarn okio.**
-dontwarn org.apache.commons.codec.binary.**

# Proguard iqy for release
-keep class com.mcto.sspsdk.** { *; }
-dontwarn com.mcto.sspsdk.**

# Proguard iqy qilin for release
-keep class XI.CA.XI.**{*;}
-keep class XI.K0.XI.**{*;}
-keep class XI.XI.K0.**{*;}
-keep class XI.xo.XI.XI.**{*;}
-keep class com.asus.msa.SupplementaryDID.**{*;}
-keep class com.asus.msa.sdid.**{*;}
-keep class com.bun.lib.**{*;}
-keep class com.bun.miitmdid.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.opendeviceidlibrary.**{*;}
-keep class org.json.**{*;}
-keep public class com.netease.nis.sdkwrapper.Utils {
    public <methods>;
}

#-------------- AdSet start-------------
-keep class com.kc.openset.**{*;}
-keep class com.od.**{*;}
-dontwarn com.kc.openset.**

-keep @com.qihoo.SdkProtected.OSETSDK.Keep class **{*;}
-keep,allowobfuscation interface com.qihoo.SdkProtected.OSETSDK.Keep
#-------------- AdSet end-------------

#-------------- gromoe start-------------
-keep class com.bytedance.sdk.openadsdk.** { *; }
-keep public interface com.bytedance.sdk.openadsdk.downloadnew.** {*;}
-keep class com.ss.**{*;}
-dontwarn com.ss.**

-keep class bykvm*.**
-keep class com.bytedance.msdk.adapter.**{ public *; }
-keep class com.bytedance.msdk.api.** {
 public *;
}
-keep class com.bytedance.msdk.base.TTBaseAd{*;}
-keep class com.bytedance.msdk.adapter.TTAbsAdLoaderAdapter{
    public *;
    protected <fields>;
}
#-keep class com.bykv.**{*;}
#-keep class com.byted.live.lite.**{*;}
#-keep class com.bytedance.**{*;}
#-keep class com.pandora.common.**{*;}
#-keep class com.ss.**{*;}
#-------------- gromoe end -------------

#-------------- 快手 start-------------
-keep class org.chromium.** {*;}
-keep class org.chromium.** { *; }
-keep class aegon.chrome.** { *; }
-keep class com.kwai.**{ *; }

-dontwarn com.kwai.**
-dontwarn com.kwad.**
-dontwarn com.ksad.**
-dontwarn aegon.chrome.**
#-------------- 快手 end-------------

#-------------- sigmob start-------------
-dontoptimize
-ignorewarnings

-keep class sun.misc.Unsafe { *; }
-keep class com.sigmob.**.**{*;}
-keep interface com.sigmob.**.**{*;}
-keep class com.czhj.**{*;}
-keep interface com.czhj.**{*;}

# androidx
-keep class com.google.android.material.** {*;}
-keep class androidx.** {*;}
-keep public class * extends androidx.**
-keep interface androidx.** {*;}
-dontwarn com.google.android.material.**
-dontnote com.google.android.material.**
-dontwarn androidx.**

# android.support.v4
-dontwarn android.support.v4.**
-keep class android.support.v4.** { *; }
-keep interface android.support.v4.** { *; }
-keep public class * extends android.support.v4.**

# ToBid
-dontwarn com.sigmob.**
-keep class com.sigmob.**{ *;}
-keep interface com.sigmob.**{ *;}

-dontwarn com.czhj.**
-keep class com.czhj.**{ *;}
-keep interface com.czhj.**{ *;}
-keep class com.tan.**{ *;}

-dontwarn com.windmill.**
-keep class com.windmill.**.**{*;}
-keep interface com.windmill.**{ *;}
#-------------- sigmob end-------------

#-------------- oaid start-------------
# 注意不同版本混淆不一样，如果你接入的是sdk默认的1.0.25版本就用下面的，其他版本自行检查
-keep class com.bun.supplier.** {*;}
-dontwarn com.bun.supplier.core.**
-keep class XI.**{*;}
-keep class com.bun.miitmdid.**{*;}

-keep class com.asus.msa.**{*;}
-keep class com.bun.**{*;}
-keep class com.huawei.hms.ads.identifier.**{*;}
-keep class com.netease.nis.sdkwrapper.**{*;}
-keep class com.samsung.android.deviceidservice.**{*;}
-keep class com.zui.**{*;}
-keep class XI.**{*;}
#-------------- oaid end-------------

#-------------- 广点通 start-------------
-keep class com.qq.e.** {*;}
-dontwarn com.qq.e.**
#-------------- 广点通 end-------------

#-------------- 百度 start-------------
-ignorewarnings
-dontwarn com.baidu.mobads.sdk.api.**
-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class com.baidu.mobads.** { *; }
-keep class com.style.widget.** {*;}
-keep class com.component.** {*;}
-keep class com.baidu.ad.magic.flute.** {*;}
-keep class com.baidu.mobstat.forbes.** {*;}
#-------------- 百度 end-------------

#-------------- okhttp3 start-------------
# OkHttp3
# https://github.com/square/okhttp
# okhttp
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.squareup.okhttp.* { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**

# okhttp 3
-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

-keepattributes InnerClasses

# Okio
-dontwarn com.squareup.**
-dontwarn okio.**
-keep public class org.codehaus.* { *; }
-keep public class java.nio.* { *; }
#----------okhttp end--------------

#----------log start--------------
-keep class com.aliyun.sls.android.producer.** { *; }
-keep interface com.aliyun.sls.android.producer.* { *; }
#----------log end--------------

#----------倍孜 start--------------
-dontwarn com.beizi.fusion.**
-dontwarn com.beizi.ad.**
-keep class com.beizi.fusion.** {*; }
-keep class com.beizi.ad.** {*; }

-keep class com.qq.e.** {
    public protected *;
}

-keepattributes Exceptions,InnerClasses,Signature,Deprecated,SourceFile,LineNumberTable,*Annotation*,EnclosingMethod

-dontwarn  org.apache.**
#----------倍孜 end--------------

#----------Glide start--------------
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.**{*;}
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
#----------Glide end--------------

#----------阿里TanxAd start--------------
-dontwarn com.alibaba.fastjson.**

-keep class com.alibaba.fastjson.**{*;}
-keep class com.bumptech.glide.**{*;}

-keep class com.alimm.tanx.**{*;}

# 有进程间通信,保证service相关不被混淆
#-keep public class * extends android.app.Service
#-keep public class * extends android.content.BroadcastReceiver

# 自动曝光数据的防混淆
-keep class * implements java.io.Serializable{
     <fields>;
    <methods>;
}
#----------阿里TanxAd end--------------

#----------华为荣耀 start--------------
-keep class com.hihonor.ads.** {*; }
#----------华为荣耀 end--------------

#-------------- 章鱼 start-------------
-dontwarn com.octopus.ad.**
-keep class com.octopus.ad.** {*;}
#-------------- 章鱼 end-------------

#-------------- 京东 start-------------
-keep class com.jd.ad.sdk.** { *; }
#-------------- 京东 end-------------

# Proguard cloud.tencent asr-realtime for release
-keepclasseswithmembernames class * {
    native <methods>;
}
-keep public class com.tencent.aai.*
-keep public class com.qq.wx.voice.*

# Proguard easypay and snackbar for release
-keep class com.chrishui.** { *; }
-dontwarn com.chrishui.**

# Proguard imagepicker for release
-keep class com.mx.imgpicker.** { *; }
-dontwarn com.mx.imgpicker.**

# Proguard photoview for release
-keep class com.github.chrisbanes.photoview.** { *; }
-dontwarn com.github.chrisbanes.photoview.**

# Proguard umeng for release
-keep class com.umeng.sdk.** { *; }
-dontwarn com.umeng.sdk.**

# Proguard EasyProtector for release
-keep class com.lahm.library.** { *; }
-dontwarn com.lahm.library.**

# Proguard oppo for release
-keep class com.nearme.** { *; }
-dontwarn com.nearme.**

# Proguard oppo ad for release
-keep class com.opos.** { *; }
-keep class com.heytap.msp.mobad.** { *; }
-keep class com.heytap.openid.** {*; }
-keep class okio.** { *; }
-keeppackagenames com.heytap.nearx.tapplugin