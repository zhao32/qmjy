package com.cocos.game

import android.app.Application
import com.lzy.okgo.OkGo
import com.lzy.okgo.cache.CacheEntity
import com.lzy.okgo.cache.CacheMode
import com.lzy.okgo.model.HttpHeaders
import com.lzy.okgo.model.HttpParams
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkGoUtil {
    fun init(app: Application) {
        val okHttpClient = OkHttpClient.Builder()
            .addInterceptor(HttpHeaderInterceptor())
            .addInterceptor(
                HttpLoggingInterceptor(HttpLoggerInterceptor())
                    .setLevel(HttpLoggingInterceptor.Level.BODY)
            )
            // 全局的读取超时时间
            .readTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            // 全局的写入超时时间
            .writeTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            // 全局的连接超时时间
            .connectTimeout(OkGo.DEFAULT_MILLISECONDS, TimeUnit.MILLISECONDS)
            .build()

        // 配置Cookie，以下任选其一
        // 1、使用【sp】保持cookie，如果cookie不过期，则一直有效
        // builder.cookieJar(new CookieJarImpl(new SPCookieStore(getContext())));
        // 2、使用【数据库】保持cookie，如果cookie不过期，则一直有效
        // builder.cookieJar(new CookieJarImpl(new DBCookieStore(getContext())));
        // 3、使用【内存】保持cookie，app退出后，cookie消失
        // builder.cookieJar(new CookieJarImpl(new MemoryCookieStore()));

        OkGo.getInstance().init(app) // 必须调用初始化
            .setOkHttpClient(okHttpClient) // 建议设置OkHttpClient，不设置将使用默认的
            .setCacheMode(CacheMode.NO_CACHE) // 全局统一缓存模式，默认不使用缓存，可以不传
            .setCacheTime(CacheEntity.CACHE_NEVER_EXPIRE) // 全局统一缓存时间，默认永不过期，可以不传
            .setRetryCount(3) // 全局统一超时重连次数，默认为三次，那么最差的情况会请求4次(一次原始请求，三次重连请求)，不需要可以设置为0
            .addCommonHeaders(HttpHeaders()) // 全局公共头
            .addCommonParams(HttpParams()) // 全局公共参数

    }
}