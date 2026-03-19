package com.cocos.game;

import android.app.Activity;
import android.app.Dialog;
import android.view.View;
import android.view.Window;

public class DialogUtil {
    private DialogUtil() {
    }

    public static void configSystemUI(Activity activity, Dialog dialog) {
        if (dialog.getWindow() == null) return;
        dialog.setOnShowListener(dialogInterface -> hideSystemUI(activity.getWindow()));
        dialog.setOnDismissListener(dialogInterface -> hideSystemUI(activity.getWindow()));
    }

    private static void hideSystemUI(Window window) {
        window.getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        );
    }
}
