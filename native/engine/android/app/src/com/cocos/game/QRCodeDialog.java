package com.cocos.game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;

import com.yourong.game.jydhm.R;
import com.yzq.zxinglibrary.encode.CodeCreator;

public class QRCodeDialog extends Dialog {
    private final Context context;

    private final String url;

    private int width = 30; // 左右边距
    private int height = 300; // 高

    public QRCodeDialog(Context context, String url) {
        super(context);
        this.context = context;
        this.url = url;
    }

    public QRCodeDialog(Context context, String url, int width, int height) {
        super(context);
        this.context = context;
        this.url = url;
        this.width = width;
        this.height = height;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        View contentView = LayoutInflater.from(context).inflate(R.layout.layout_qrcode_dialog, null);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setContentView(contentView);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = getScreenWidth((Activity) context) - dp2px(context, width) * 2;
        layoutParams.height = dp2px(context, height);
        window.setAttributes(layoutParams);

        initView();
    }

    private void initView() {
        findViewById(R.id.save).setOnClickListener(v -> {
            FileUtil.saveBitmap(context, FileUtil.viewToBitmap(findViewById(R.id.image)));
            dismiss();
        });

        ((Activity) context).runOnUiThread(() -> {
            ImageView image = findViewById(R.id.image);
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Bitmap shareInfoQRCodeBm = CodeCreator.createQRCode(url, 300, 300, logo);
            image.setImageBitmap(shareInfoQRCodeBm);
        });
    }

    private int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private int getScreenWidth(Activity activity) {
        DisplayMetrics dm = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(dm);
        return dm.widthPixels;
    }
}
