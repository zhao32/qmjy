package com.cocos.game;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.yourong.game.jydhm.R;
import com.yzq.zxinglibrary.encode.CodeCreator;

import java.io.ByteArrayOutputStream;

public class QRCodeFullscreenDialog extends Dialog {
    private final Context context;
    private View contentView;

    private final String provider;
    private final String avatar;
    private final String inviteCode;
    private final String nickname;
    private final String content;

    public QRCodeFullscreenDialog(Context context, String provider, String avatar, String inviteCode, String nickname, String content) {
        super(context);
        this.context = context;
        this.provider = provider;
        this.avatar = avatar;
        this.inviteCode = inviteCode;
        this.nickname = nickname;
        this.content = content;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        contentView = LayoutInflater.from(context).inflate(R.layout.layout_qrcode_fullscreen_dialog, null);

        Window window = getWindow();
        window.setBackgroundDrawableResource(android.R.color.transparent);
        window.setGravity(Gravity.CENTER);
        window.getDecorView().setPadding(0, 0, 0, 0);
        window.setContentView(contentView);
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        window.setAttributes(layoutParams);

        contentView.setVisibility(View.INVISIBLE);
        initView();
    }

    private void initView() {
        ImageView avatarIv = findViewById(R.id.avatarIv);
        TextView inviteCodeTv = findViewById(R.id.inviteCodeTv);
        TextView nicknameTv = findViewById(R.id.nicknameTv);
        ImageView qrcodeIv = findViewById(R.id.qrcodeIv);

        Glide.with(context).load(avatar)
                .placeholder(R.mipmap.ic_qrcode_fullscreen_4)
                .error(R.mipmap.ic_qrcode_fullscreen_4)
                .fallback(R.mipmap.ic_qrcode_fullscreen_4)
                .transform(new CenterCrop())
                .into(avatarIv);
        inviteCodeTv.setText(String.format("邀请码：%s", inviteCode));
        nicknameTv.setText(nickname);

        LoadingDialog.getInstance().show();
        ((Activity) context).runOnUiThread(() -> {
            Bitmap logo = BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher);
            Bitmap shareInfoQRCodeBm = CodeCreator.createQRCode(content, 300, 300, logo);
            qrcodeIv.setImageBitmap(shareInfoQRCodeBm);

            contentView.postDelayed(() -> {
                Bitmap bitmap = FileUtil.viewToBitmap(contentView);
                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
                byte[] byteArray = byteArrayOutputStream.toByteArray();
                String s = Base64.encodeToString(byteArray, Base64.DEFAULT);
                JSONObject data = new JSONObject();
                data.put("provider", provider);
                data.put("type", "4");
                data.put("image", s);
                JSONObject event = new JSONObject();
                event.put("event", "share");
                event.put("data", data);
                JsbBridgeCallback.getInstance().onScript(JSON.toJSONString(event), "");
                contentView.postDelayed(() -> {
                    LoadingDialog.getInstance().hide();
                    dismiss();
                }, 300);
            }, 300);
        });
    }
}
