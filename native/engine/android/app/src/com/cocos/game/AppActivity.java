/****************************************************************************
 Copyright (c) 2015-2016 Chukong Technologies Inc.
 Copyright (c) 2017-2018 Xiamen Yaji Software Co., Ltd.

 http://www.cocos2d-x.org

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 ****************************************************************************/
package com.cocos.game;

import android.app.Application;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebView;

import androidx.annotation.NonNull;

import com.cocos.lib.CocosActivity;
import com.cocos.service.SDKWrapper;
import com.umeng.sdk.UMengUtil;
import com.yourong.game.jydhm.BuildConfig;

public class AppActivity extends CocosActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        // DO OTHER INITIALIZATION BELOW
        SDKWrapper.shared().init(this);

        // 网络
        OkGoUtil.INSTANCE.init(getApplication());
        // 选图
        MXPickerUtil.INSTANCE.init(getApplication());
        // Cocos 交互接口
        JsbBridgeCallback.getInstance().init(this);

        // taku
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            String processName = Application.getProcessName();
            if (!getPackageName().equals(processName)) {
                WebView.setDataDirectorySuffix(processName);
            }
        }
    }

    // Cocos 交互接口
    public static void sendToNative(String req, String arg) {
        JsbBridgeCallback.getInstance().onScript(req, arg);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SDKWrapper.shared().onResume();
        JsbBridgeCallback.getInstance().onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SDKWrapper.shared().onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Workaround in https://stackoverflow.com/questions/16283079/re-launch-of-activity-on-home-button-but-only-the-first-time/16447508
        if (!isTaskRoot()) {
            return;
        }
        SDKWrapper.shared().onDestroy();
        JsbBridgeCallback.getInstance().onDestroy();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        SDKWrapper.shared().onNewIntent(intent);
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        SDKWrapper.shared().onRestart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        SDKWrapper.shared().onStop();
    }

    @Override
    public void onBackPressed() {
        Logger.e("", String.format("onBackPressed：%s, %s", BuildConfig.KEYBACK_MODE, getGameActivityNativeHandle()));
        if (TextUtils.equals(BuildConfig.KEYBACK_MODE, "always")) {
            SDKWrapper.shared().onBackPressed();
            super.onBackPressed();
            finish();
            QuitUtil.exitApp(this);
        } else if (TextUtils.equals(BuildConfig.KEYBACK_MODE, "ask")) {
            if (FcmUtil.getInstance().isInitedOppoGame()) {
                FcmUtil.getInstance().exitOppoGame(this);
            } else {
                QuitDialog.getInstance().show(this);
            }
        } else if (TextUtils.equals(BuildConfig.KEYBACK_MODE, "never")) {
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            onBackPressed();
            return true;
        } else return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        SDKWrapper.shared().onConfigurationChanged(newConfig);
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        SDKWrapper.shared().onRestoreInstanceState(savedInstanceState);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        SDKWrapper.shared().onSaveInstanceState(outState);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStart() {
        SDKWrapper.shared().onStart();
        super.onStart();
    }

    @Override
    public void onLowMemory() {
        SDKWrapper.shared().onLowMemory();
        super.onLowMemory();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        SDKWrapper.shared().onActivityResult(requestCode, resultCode, data);
        MXPickerUtil.INSTANCE.onActivityResult(requestCode, resultCode, data);
        UMengUtil.getInstance().onActivityResult(requestCode, resultCode, data);
        ScanCodeUtil.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        MXPickerUtil.INSTANCE.onRequestPermissionsResult(requestCode, grantResults);
        AsrRealtimeUtil.getInstance().onRequestPermissionsResult(requestCode, grantResults);
        MakePhoneCallUtil.getInstance().onRequestPermissionsResult(requestCode, grantResults);
        ScanCodeUtil.getInstance().onRequestPermissionsResult(requestCode, grantResults);
        FileUtil.onRequestPermissionsResult(requestCode, grantResults);
    }

}
