package com.cocos.game;

import android.webkit.WebSettings;

import androidx.annotation.NonNull;

import com.cocos.service.SDKWrapper;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

public class HttpHeaderInterceptor implements Interceptor {

    @NonNull
    @Override
    public Response intercept(@NonNull Chain chain) throws IOException {
        Request request = chain.request();
        // 修改信息：加入公共的 Header 信息
        Request.Builder newBuilder = request.newBuilder()
                .headers(request.headers())
                .removeHeader("User-Agent")
                .addHeader("Connection", "keep-alive")
                .addHeader("User-Agent", UserAgent.getUserAgent());
        Request newRequest = newBuilder.build();

        // 执行修改后的请求
        return chain.proceed(newRequest);
    }

    private static class UserAgent {
        static String getUserAgent() {
            String userAgent = "";
            try {
                userAgent = WebSettings.getDefaultUserAgent(SDKWrapper.shared().getActivity());
            } catch (Exception e) {
                userAgent = System.getProperty("http.agent");
            }
            StringBuilder sb = new StringBuilder();
            for (int i = 0, length = userAgent.length(); i < length; i++) {
                char c = userAgent.charAt(i);
                if (c <= '\u001f' || c >= '\u007f') {
                    sb.append(String.format("\\u%04x", (int) c));
                } else {
                    sb.append(c);
                }
            }
            return sb.toString();
        }
    }
}
