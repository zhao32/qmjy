package com.cocos.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.yourong.game.jydhm.R;

public class LoadingDialog {
    private Activity activity;
    private AlertDialog loadingDialog;

    private LoadingDialog() {
    }

    private static final class InstanceHolder {
        private static final LoadingDialog mInstance = new LoadingDialog();
    }

    public static LoadingDialog getInstance() {
        return InstanceHolder.mInstance;
    }

    public void init(Activity activity) {
        this.activity = activity;
    }

    public void show() {
        if (loadingDialog == null) {
            loadingDialog = new AlertDialog.Builder(activity).create();
            if (loadingDialog.getWindow() != null)
                loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable());
            loadingDialog.setCancelable(false);
            loadingDialog.setOnKeyListener((dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_SEARCH || keyCode == KeyEvent.KEYCODE_BACK);
            loadingDialog.setCanceledOnTouchOutside(false);
        }
        if (loadingDialog.isShowing()) return;
        DialogUtil.configSystemUI(activity, loadingDialog);
        loadingDialog.show();

        ImageView rotateIv = new ImageView(activity);
        rotateIv.setImageResource(R.drawable.loading);
        FrameLayout.LayoutParams lpIv = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        lpIv.gravity = Gravity.CENTER;

        FrameLayout rotateRoot = new FrameLayout(activity);
        FrameLayout.LayoutParams lpRoot = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        rotateRoot.setLayoutParams(lpRoot);
        rotateRoot.addView(rotateIv, lpIv);

        RotateAnimation animation = new RotateAnimation(0, 360,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f);
        animation.setDuration(800);
        animation.setFillAfter(false);
        animation.setRepeatCount(Animation.INFINITE);
        animation.setRepeatMode(Animation.RESTART);
        animation.setInterpolator(new LinearInterpolator());
        rotateIv.clearAnimation();
        rotateIv.startAnimation(animation);

        loadingDialog.setContentView(rotateRoot);
    }

    public void hide() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }
}
