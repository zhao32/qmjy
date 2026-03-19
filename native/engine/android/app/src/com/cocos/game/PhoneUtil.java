package com.cocos.game;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityManager;
import android.webkit.WebSettings;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.lahm.library.EasyProtectorLib;
import com.yourong.game.jydhm.BuildConfig;

import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PhoneUtil {
    private PhoneUtil() {
    }

    /**
     * package: 当前应用的包名,
     * versionCode: 当前应用的版本号,
     * versionName: 当前应用的版本名,
     * manufacturer: 设备制造商,
     * model: 设备型号,
     * brand: 设备品牌,
     * product: 产品名称,
     * uiOsVersion: 系统版本,
     * apiVersion: SDK版本号,
     * cpu: cpu名字, // X
     * baseBand: 基带版本,
     * pixels: 设备分辨率,
     * dpi: 屏幕密度,
     * tablet: 是否平板,
     * androidVersion: 安卓版本号,
     * androidId: 安卓ID,
     * oaid: 安卓OAID,
     * mac: mac地址, // 可能是虚拟的
     * sim: 是否有手机卡以及手机号,
     * location: 用户位置坐标, // X
     * emulator: 是否模拟器,
     * accessibility: 是否开启无障碍服务,
     * developer: 是否开启开发者模式,
     * userAgent: UserAgent,
     * <br>
     * {"package":"com.yourong.game.***","versionCode":1,"versionName":"1.0.1","manufacturer":"HUAWEI","model":"CLT-AL00",
     * "brand":"HUAWEI","product":"CLT-AL00","uiOsVersion":"EMUI EmotionUI_9.1.0","apiVersion":"28","cpu":"",
     * "baseBand":"21C20B369S009C000,21C20B369S009C000","pixels":"1080:2037","dpi":"408","tablet":"false",
     * "androidVersion":"9","androidId":"d116b44ae814d0ff","oaid":"2f9fcf7f-fe7f-c9c6-ff75-dff6dd718ad6",
     * "mac":"02:00:00:00:00:00","sim":"false","location":"","emulator":"false","accessibility":"false","developer":"true",
     * "userAgent":"Mozilla/5.0 (Linux; Android 9; CLT-AL00 Build/HUAWEICLT-AL00; wv) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/74.0.3729.136 Mobile Safari/537.36"}
     */
    public static String getPhone(Context context) {
        JSONObject data = new JSONObject();
        data.put("package", context.getPackageName());
        data.put("versionCode", BuildConfig.VERSION_CODE);
        data.put("versionName", BuildConfig.VERSION_NAME);
        data.put("manufacturer", Build.MANUFACTURER);
        data.put("model", Build.MODEL);
        data.put("brand", Build.BRAND);
        data.put("product", Build.PRODUCT);
        data.put("uiOsVersion", CustomOSUtil.getPhoneSystem(Build.BRAND));
        data.put("apiVersion", String.valueOf(Build.VERSION.SDK_INT));
        data.put("cpu", getCpuName());
        data.put("baseBand", getBasebandVersion());
        data.put("pixels", getPixels(context));
        data.put("dpi", getDensityDpi(context));
        data.put("tablet", isTablet(context));
        data.put("androidVersion", Build.VERSION.RELEASE);
        data.put("androidId", getAndroidId(context));
        data.put("oaid", JsbBridgeCallback.getInstance().getOaid(context));
        data.put("mac", getMac(context));
        data.put("sim", getSim(context));
        data.put("location", getLocation(context));
        data.put("emulator", String.valueOf(EasyProtectorLib.checkIsRunningInEmulator(context, null)));
        data.put("accessibility", String.valueOf(isAccessibilityServiceEnabled(context)));
        data.put("developer", String.valueOf(isDeveloperModeEnabled(context)));
        data.put("userAgent", WebSettings.getDefaultUserAgent(context));
        return JSON.toJSONString(data);
    }

    private static String getCpuName() {
        return "";
    }

    private static String getBasebandVersion() {
        return getSystemProperties("gsm.version.baseband");
    }

    private static String getPixels(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return TextUtils.join(":", new Integer[]{metrics.widthPixels, metrics.heightPixels});
    }

    private static String getDensityDpi(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return String.valueOf(metrics.densityDpi);
    }

    private static String isTablet(Context context) {
        int configuration = context.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK;
        boolean tablet = configuration >= Configuration.SCREENLAYOUT_SIZE_LARGE;
        return String.valueOf(tablet);
    }

    @SuppressLint("HardwareIds")
    private static String getAndroidId(Context context) {
        String systemId = "";
        try {
            systemId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (!TextUtils.isEmpty(systemId)) {
            return systemId;
        }

        String secureId = "";
        try {
            secureId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return secureId;
    }

    private static String getMac(Context context) {
        return getMacWifi(context);
    }

    @SuppressLint("HardwareIds")
    private static String getMacWifi(Context context) {
        String macAddress = "";
        try {
            WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            macAddress = connectionInfo.getMacAddress();

            if (TextUtils.isEmpty(macAddress)) {
                ArrayList<NetworkInterface> networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
                for (NetworkInterface networkInterface : networkInterfaces) {
                    if ("wlan0".equalsIgnoreCase(networkInterface.getName()) || "eth0".equalsIgnoreCase(networkInterface.getName())) {
                        byte[] hardware = networkInterface.getHardwareAddress();
                        if (hardware != null && hardware.length > 0) {
                            StringBuilder builder = new StringBuilder();
                            for (byte b : hardware) {
                                builder.append(String.format("%02X:", b));
                            }
                            if (builder.length() > 0) {
                                builder.deleteCharAt(builder.length() - 1);
                            }
                            macAddress = builder.toString();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return macAddress;
    }

    private static String getSim(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        boolean ready = telephonyManager.getSimState() == TelephonyManager.SIM_STATE_READY;
        return String.valueOf(ready);
    }

    private static String getLocation(Context context) {
        return "";
    }

    private static boolean isAccessibilityServiceEnabled(Context context) {
        boolean isAccessibilityServiceEnabled = false;
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (accessibilityManager.isEnabled()) {
            List<AccessibilityServiceInfo> enabledServices =
                    accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
            isAccessibilityServiceEnabled = enabledServices.stream()
                    .anyMatch(match -> TextUtils.equals(context.getPackageName(), match.getId()));
        }
        return isAccessibilityServiceEnabled;
    }

    private static boolean isDeveloperModeEnabled(Context context) {
        String value = getSystemProperties("persist.sys.usb.config");
        boolean isUsbEnabled = TextUtils.equals("adb", value);
        boolean isDevelopmentEnabled =
                Settings.Secure.getInt(context.getContentResolver(), Settings.Global.DEVELOPMENT_SETTINGS_ENABLED, 0) != 0;
        boolean isAdbEnabled = Settings.Secure.getInt(context.getContentResolver(), Settings.Global.ADB_ENABLED, 0) != 0;
        return isUsbEnabled || isDevelopmentEnabled || isAdbEnabled;
    }

    private static String getSystemProperties(String key) {
        String value = "";
        try {
            @SuppressLint("PrivateApi")
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(clazz, key, ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }
}
