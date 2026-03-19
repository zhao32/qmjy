package com.cocos.game;

import android.annotation.SuppressLint;
import android.os.Build;
import android.text.TextUtils;

import java.lang.reflect.Method;

public class CustomOSUtil {
    private CustomOSUtil() {
    }

    /**
     * customOS 默认值为 "", 如果识别出的手机厂商是预知的, 会被重新赋值,
     * 如果未识别到该机型则返回原生安卓信息
     */
    private static String customOS = "";

    /**
     * CustomOSVersion 默认值为 "", 如果识别出的手机厂商是预知的, 会被重新赋值成对应 rom 系统的版本号,
     * 如果未识别到该机型则返回原生安卓信息
     */
    private static String customOSVersion = "";

    /**
     * HarmonyOS 系统输出的
     * 格式：2.0.0
     */
    private static final String KEY_HARMONYOS_VERSION_NAME = "hw_sc.build.platform.version";

    /**
     * EMUI系统输出的
     * 格式：EmotionUI_8.0.0
     */
    private static final String KEY_EMUI_VERSION_NAME = "ro.build.version.emui";

    /**
     * MagicUI 系统输出的
     * 格式：3.1.0
     */
    private static final String KEY_MAGICUI_VERSION = "ro.build.version.magic";

    /**
     * MIUI 系统输出的
     * 格式：V12
     */
    private static final String KEY_MIUI_VERSION_NAME = "ro.miui.ui.version.name";

    /**
     * OPPO 手机 ColorOS 系统输出的
     * 格式：9
     */
    private static final String KEY_COLOROS_VERSION_NAME = "ro.build.version.opporom";

    /**
     * VIVO 手机系统输出的
     * name 格式：funtouch
     * version 格式： 9
     */
    private static final String KEY_VIVO_VERSION_NAME = "ro.vivo.os.name";
    private static final String KEY_VIVO_VERSION = "ro.vivo.os.version";

    /**
     * OonPlus 手机系统输出的
     * 格式：Hydrogen OS 11.0.7.10.KB05
     */
    private static final String KEY_ONEPLUS_VERSION_NAME = "ro.rom.version";

    /**
     * 魅族手机系统输出的
     */
    private static final String KEY_FLYME_VERSION_NAME = "ro.build.display.id";

    /**
     * nubia 手机系统输出的
     */
    private static final String KEY_NUBIA_VERSION_NAME = "ro.build.nubia.rom.name";
    private static final String KEY_NUBIA_VERSION_CODE = "ro.build.nubia.rom.code";

    /**
     * 传入获取手机系统属性的 key, 可以得到 rom 系统版本信息
     */
    @SuppressLint("PrivateApi")
    private static String getSystemPropertyValue(String key) {
        String value = "";
        try {
            Class<?> classType = Class.forName("android.os.SystemProperties");
            Method getMethod = classType.getDeclaredMethod("get", String.class);
            value = (String) getMethod.invoke(classType, new Object[]{key});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 判断是否是华为鸿蒙系统, 能否识别荣耀鸿蒙未知
     */
    private static boolean isHarmonyOS() {
        try {
            Class<?> classType = Class.forName("com.huawei.system.BuildEx");
            Method getMethod = classType.getMethod("getOsBrand");
            String value = (String) getMethod.invoke(classType);
            return !TextUtils.isEmpty(value);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static String getHarmonySystemPropertyValue() {
        String value = "";
        try {
            Class<?> classType = Class.forName("com.huawei.system.BuildEx");
            Method getMethod = classType.getMethod("getOsBrand");
            value = (String) getMethod.invoke(classType);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    /**
     * 通过手机品牌信息获取手机 rom 系统+系统版本号
     */
    public static String getPhoneSystem(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return String.format("%s %s", customOS, customOSVersion);
    }

    private static boolean isMagicUI() {
        return false;
    }

    /**
     * 通过手机品牌信息获取手机 rom 系统
     */
    public static String getCustomOS(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return customOS;
    }

    /**
     * 通过手机品牌信息获取手机 rom 系统版本号
     */
    public static String getCustomOSVersion(String phoneBrand) {
        if (TextUtils.isEmpty(customOS)) {
            setCustomOSInfo(phoneBrand);
        }
        return customOSVersion;
    }

    /**
     * 通过手机品牌信息获取手机 rom 系统版本号, 截取成大版本, 比如 2.0.0 或者 2
     */
    public static String getCustomOSVersionSimple(String phoneBrand) {
        String customOSVersionSimple = customOSVersion;
        if (TextUtils.isEmpty(customOS)) {
            getCustomOSVersion(phoneBrand);
        }
        if (customOSVersion.contains(".")) {
            int index = customOSVersion.indexOf(".");
            customOSVersionSimple = customOSVersion.substring(0, index);
        }
        return customOSVersionSimple;
    }

    /**
     * 删除字符串中的空格并全部转成大写
     */
    private static String deleteSpaceAndToUpperCase(String str) {
        if (TextUtils.isEmpty(str)) {
            return "";
        }
        return str.replaceAll(" ", "").toUpperCase();
    }

    private static void setCustomOSInfo(String phoneBrand) {
        try {
            switch (deleteSpaceAndToUpperCase(phoneBrand)) {
                case "HUAWEI":
                    if (isHarmonyOS()) {
                        customOSVersion = getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME);
                        customOS = "HarmonyOS";
                    } else {
                        customOS = "EMUI";
                        customOSVersion = getSystemPropertyValue(KEY_EMUI_VERSION_NAME);
                    }
                    break;
                case "HONOR":
                    if (isHarmonyOS()) {
                        customOS = "HarmonyOS";
                        if (!TextUtils.isEmpty(getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME))) {
                            customOSVersion = getSystemPropertyValue(KEY_HARMONYOS_VERSION_NAME);
                        } else {
                            customOSVersion = "";
                        }
                    } else if (!TextUtils.isEmpty(getSystemPropertyValue(KEY_MAGICUI_VERSION))) {
                        customOS = "MagicUI";
                        customOSVersion = getSystemPropertyValue(KEY_MAGICUI_VERSION);
                    } else {
                        // 格式：EmotionUI_8.0.0
                        customOS = "EMUI";
                        customOSVersion = getSystemPropertyValue(KEY_EMUI_VERSION_NAME);
                    }
                    break;
                case "XIAOMI":
                case "REDMI":
                    // 格式：MIUIV12
                    customOS = "MIUI";
                    customOSVersion = getSystemPropertyValue(KEY_MIUI_VERSION_NAME);
                    break;
                case "REALME":
                case "OPPO":
                    // 格式：ColorOSV2.1
                    customOS = "ColorOS";
                    customOSVersion = getSystemPropertyValue(KEY_COLOROS_VERSION_NAME);
                    break;
                case "VIVO":
                    // 格式：Funtouch9
                    customOS = "Funtouch";
                    customOSVersion = getSystemPropertyValue(KEY_VIVO_VERSION);
                    break;
                case "ONEPLUS":
                    // 格式：Hydrogen OS 11.0.7.10.KB05
                    customOS = "HydrogenOS";
                    customOSVersion = getSystemPropertyValue(KEY_ONEPLUS_VERSION_NAME);
                    break;
                case "MEIZU":
                    // 格式:Flyme 6.3.5.1G
                    customOS = "Flyme";
                    customOSVersion = getSystemPropertyValue(KEY_FLYME_VERSION_NAME);
                    break;
                case "NUBIA":
                    // 格式:nubiaUIV3.0
                    customOS = getSystemPropertyValue(KEY_NUBIA_VERSION_NAME);
                    customOSVersion = getSystemPropertyValue(KEY_NUBIA_VERSION_CODE);
                    break;
                default:
                    customOS = "Android";
                    customOSVersion = Build.VERSION.RELEASE;
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
