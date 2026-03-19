package com.cocos.game;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import androidx.core.content.ContextCompat;

public class NetworkUtil {
    private NetworkUtil() {
    }

    public static String getNetworkType(Context context) {
        if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) &&
                (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED))
            return "none";

        ConnectivityManager connectivityManager = ContextCompat.getSystemService(context, ConnectivityManager.class);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            if (activeNetworkInfo != null) {
                return getActiveNetworkType(context, connectivityManager.getActiveNetworkInfo());
            }
        }
        return "none";
    }

    private static String getActiveNetworkType(Context context, NetworkInfo networkInfo) {
        if (networkInfo != null) {
            Logger.e("", String.format("type:%s, typeName:%s, subtype:%s, subtypeName:%s",
                    networkInfo.getType(), networkInfo.getTypeName(),
                    networkInfo.getSubtype(), networkInfo.getSubtypeName()));
            int type = networkInfo.getType();
            String typeName = networkInfo.getTypeName();
            if (type == ConnectivityManager.TYPE_WIFI || "WIFI".equalsIgnoreCase(typeName)) {
                return getWifiNetworkType(context);
            } else if (type == ConnectivityManager.TYPE_MOBILE) {
                int subtype = networkInfo.getSubtype();
                String subtypeName = networkInfo.getSubtypeName();
                return getMobileNetworkType(subtype, subtypeName);
            } else if (type == ConnectivityManager.TYPE_BLUETOOTH) {
                return "bluetooth";
            } else if (type == ConnectivityManager.TYPE_ETHERNET) {
                return "ethernet";
            } else {
                return TextUtils.isEmpty(typeName) ? "unknown" : typeName;
            }
        }
        return "unknown";
    }

    private static String getWifiNetworkType(Context context) {
        WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiManager != null) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            if (wifiInfo != null && wifiInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                return "wifi";
            }
        }
        return "unknown";
    }

    private static String getMobileNetworkType(int subtype, String subtypeName) {
        return switch (subtype) {
            case TelephonyManager.NETWORK_TYPE_GPRS, TelephonyManager.NETWORK_TYPE_EDGE,
                 TelephonyManager.NETWORK_TYPE_CDMA, TelephonyManager.NETWORK_TYPE_1xRTT -> "2g";
            case TelephonyManager.NETWORK_TYPE_UMTS, TelephonyManager.NETWORK_TYPE_EVDO_0,
                 TelephonyManager.NETWORK_TYPE_EVDO_A, TelephonyManager.NETWORK_TYPE_HSDPA,
                 TelephonyManager.NETWORK_TYPE_EVDO_B, TelephonyManager.NETWORK_TYPE_EHRPD,
                 TelephonyManager.NETWORK_TYPE_TD_SCDMA, TelephonyManager.NETWORK_TYPE_IWLAN -> "3g";
            case TelephonyManager.NETWORK_TYPE_IDEN, TelephonyManager.NETWORK_TYPE_LTE,
                 TelephonyManager.NETWORK_TYPE_HSUPA, TelephonyManager.NETWORK_TYPE_HSPA,
                 TelephonyManager.NETWORK_TYPE_HSPAP -> "4g";
            case TelephonyManager.NETWORK_TYPE_NR -> "5g";
            default -> TextUtils.isEmpty(subtypeName) ? "unknown" : subtypeName;
        };
    }
}
