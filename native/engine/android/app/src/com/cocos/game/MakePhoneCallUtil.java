package com.cocos.game;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MakePhoneCallUtil {
    private Context context;
    private String event;
    private JSONObject data;

    private MakePhoneCallUtil() {
    }

    private static final class InstanceHolder {
        private static final MakePhoneCallUtil mInstance = new MakePhoneCallUtil();
    }

    public static MakePhoneCallUtil getInstance() {
        return MakePhoneCallUtil.InstanceHolder.mInstance;
    }

    public void handler(Context context, String event, JSONObject data) {
        String phoneNumber = data.getString("phoneNumber");
        if (!TextUtils.isEmpty(phoneNumber)) {
            String action = data.getString("action");
            if (TextUtils.equals("call", action)) {
                String[] permissions = {Manifest.permission.CALL_PHONE};
                if (hasPermission(context, permissions)) {
                    Uri callUri = Uri.parse(String.format("tel:%s", phoneNumber));
                    Intent callIntent = new Intent(Intent.ACTION_CALL, callUri);
                    context.startActivity(callIntent);
                } else {
                    this.context = context;
                    this.event = event;
                    this.data = data;
                    ActivityCompat.requestPermissions((Activity) context, permissions, 0x77);
                }
            } else {
                Uri dialUri = Uri.parse(String.format("tel:%s", phoneNumber));
                Intent dialIntent = new Intent(Intent.ACTION_DIAL, dialUri);
                context.startActivity(dialIntent);
            }
        }
    }

    public void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 0x77) {
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
