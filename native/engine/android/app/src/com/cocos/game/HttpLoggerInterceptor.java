package com.cocos.game;

import org.jetbrains.annotations.NotNull;


public class HttpLoggerInterceptor implements okhttp3.logging.HttpLoggingInterceptor.Logger {
    private static final String TAG = HttpLoggerInterceptor.class.getSimpleName();

    @Override
    public void log(@NotNull String message) {
        Logger.e(TAG, message);
    }
}
