package com.cocos.game;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Build;
import android.os.Process;

public class QuitUtil {
    private QuitUtil() {
    }

    public static void exitApp(Context context) {
        try {
            ActivityManager activityManager;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                activityManager = context.getSystemService(ActivityManager.class);
            } else {
                activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                activityManager.getAppTasks().forEach(ActivityManager.AppTask::finishAndRemoveTask);
            }
            Process.killProcess(Process.myPid());
            System.exit(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
