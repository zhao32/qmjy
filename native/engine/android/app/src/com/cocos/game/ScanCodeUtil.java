package com.cocos.game;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson2.JSONObject;
import com.yzq.zxinglibrary.android.CaptureActivity;
import com.yzq.zxinglibrary.bean.ZxingConfig;
import com.yzq.zxinglibrary.common.Constant;

import java.util.ArrayList;
import java.util.List;

public class ScanCodeUtil {
    private Activity context;
    private String event;
    private JSONObject data;

    private ScanCodeUtil() {
    }

    private static final class InstanceHolder {
        private static final ScanCodeUtil mInstance = new ScanCodeUtil();
    }

    public static ScanCodeUtil getInstance() {
        return ScanCodeUtil.InstanceHolder.mInstance;
    }

    public void handler(Activity context, String event, JSONObject data) {
        String[] permissions = {Manifest.permission.CAMERA};
        if (hasPermission(context, permissions)) {
            Intent intent = new Intent(context, CaptureActivity.class);

            ZxingConfig config = new ZxingConfig();
            // 是否播放声音 默认为true
            boolean beep = data.getBooleanValue("beep", true);
            config.setPlayBeep(beep);
            // 是否震动  默认为true
            boolean shake = data.getBooleanValue("shake", true);
            config.setShake(shake);
            // 是否扫描条形码 默认为true
            boolean bar = data.getBooleanValue("bar", true);
            config.setDecodeBarCode(bar);
            // 是否全屏扫描 默认为true 设为false则只会在扫描框中扫描
            boolean full = data.getBooleanValue("full", true);
            config.setFullScreenScan(full);
            // 是否显示相册 默认为true
            boolean album = data.getBooleanValue("album", true);
            config.setShowAlbum(album);
            // 是否显示下方的其他功能（闪光灯（设备支持就显示, 否则不显示）、相册） 默认为true
            boolean bottom = data.getBooleanValue("bottom", true);
            config.setShowbottomLayout(bottom);
            intent.putExtra(Constant.INTENT_ZXING_CONFIG, config);

            context.startActivityForResult(intent, 0x99);
        } else {
            this.context = context;
            this.event = event;
            this.data = data;
            ActivityCompat.requestPermissions(context, permissions, 0x88);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // 扫描二维码/条码结果
        if (requestCode == 0x99 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                String content = data.getStringExtra(Constant.CODED_CONTENT);
                JsbBridgeCallback.getInstance().sendToScript(event, content);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 0x88) {
            List<Integer> list = new ArrayList<>();
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) list.add(grantResult);
            }
            if (list.isEmpty()) {
                handler(context, event, data);
            }
        }
    }

    private boolean hasPermission(Context context, String[] array) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> list = new ArrayList<>();
        for (String s : array) {
            if (ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                list.add(s);
            }
        }
        return list.isEmpty();
    }
}
