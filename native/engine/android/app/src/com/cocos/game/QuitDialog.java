package com.cocos.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.yourong.game.jydhm.R;

public class QuitDialog {
    private AlertDialog loadingDialog;

    private QuitDialog() {
    }

    private static final class InstanceHolder {
        private static final QuitDialog mInstance = new QuitDialog();
    }

    public static QuitDialog getInstance() {
        return InstanceHolder.mInstance;
    }

    public void show(Activity activity) {
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

        float dp10 = dp2px(activity, 10F);
        float dp15 = dp2px(activity, 15F);
        float dp20 = dp2px(activity, 20F);
        float dp30 = dp2px(activity, 30F);
        float dp50 = dp2px(activity, 50F);
        int matchParent = FrameLayout.LayoutParams.MATCH_PARENT;
        int wrapContent = FrameLayout.LayoutParams.WRAP_CONTENT;

        CardView rootCv = new CardView(activity);
        rootCv.setRadius(dp10);
        rootCv.setCardBackgroundColor(Color.WHITE);

        LinearLayout rootLv = new LinearLayout(activity);
        rootLv.setOrientation(LinearLayout.VERTICAL);
        rootLv.setGravity(Gravity.CENTER_HORIZONTAL);
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(matchParent, matchParent);
        rootCv.addView(rootLv, layoutParams);

        TextView title = new TextView(activity);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
        title.setTextColor(Color.BLACK);
        title.setText(activity.getString(R.string.app_name));

        layoutParams = new FrameLayout.LayoutParams(wrapContent, wrapContent);
        layoutParams.setMargins(0, (int) dp20, 0, 0);
        rootLv.addView(title, layoutParams);

        TextView message = new TextView(activity);
        message.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
        message.setTextColor(Color.GRAY);
        message.setText("退出游戏？");

        layoutParams = new FrameLayout.LayoutParams(wrapContent, wrapContent);
        layoutParams.setMargins(0, (int) dp15, 0, 0);
        rootLv.addView(message, layoutParams);

        LinearLayout btnLv = new LinearLayout(activity);
        btnLv.setOrientation(LinearLayout.HORIZONTAL);
        btnLv.setGravity(Gravity.CENTER_HORIZONTAL);
        layoutParams = new FrameLayout.LayoutParams(matchParent, wrapContent);
        layoutParams.setMargins(0, (int) dp30, 0, (int) dp20);
        rootLv.addView(btnLv, layoutParams);

        int btnPaddingH = (int) dp15;
        int btnPaddingV = (int) dp10;

        TextView cancel = new TextView(activity);
        cancel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        cancel.setTextColor(Color.GRAY);
        cancel.setText("取消");
        cancel.setPadding(btnPaddingH, btnPaddingV, btnPaddingH, btnPaddingV);
        cancel.setOnClickListener(view -> hide());

        layoutParams = new FrameLayout.LayoutParams(wrapContent, wrapContent);
        btnLv.addView(cancel, layoutParams);

        CardView confirmCv = new CardView(activity);
        confirmCv.setRadius(dp10);
        confirmCv.setCardBackgroundColor(Color.YELLOW);
        confirmCv.setCardElevation(0F);
        confirmCv.setOnClickListener(view -> {
            activity.finish();
            loadingDialog = null;
            QuitUtil.exitApp(activity);
        });

        layoutParams = new FrameLayout.LayoutParams(wrapContent, wrapContent);
        layoutParams.setMargins((int) dp30, 0, 0, 0);
        btnLv.addView(confirmCv, layoutParams);

        TextView confirm = new TextView(activity);
        confirm.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        confirm.setTextColor(Color.BLACK);
        confirm.setText("退出");
        confirm.setPadding(btnPaddingH, btnPaddingV, btnPaddingH, btnPaddingV);

        layoutParams = new FrameLayout.LayoutParams(wrapContent, wrapContent);
        confirmCv.addView(confirm, layoutParams);

        Point screen = getScreen(activity);
        layoutParams = new FrameLayout.LayoutParams((int) (screen.x - dp50 * 2), wrapContent);

        loadingDialog.setContentView(rootCv, layoutParams);
    }

    public void hide() {
        if (null != loadingDialog && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    private Point getScreen(Activity activity) {
        WindowManager wm = ((WindowManager) activity.getSystemService(Context.WINDOW_SERVICE));
        Display display;
        if (wm != null) {
            display = wm.getDefaultDisplay();
        } else {
            return null;
        }

        Point screenSize = new Point();
        if (display != null) {
            display.getRealSize(screenSize);
        } else {
            return null;
        }
        return screenSize;
    }

    private float dp2px(Activity activity, float dp) {
        final float scale = activity.getResources().getDisplayMetrics().density;
        return dp * scale + 0.5f;
    }
}
