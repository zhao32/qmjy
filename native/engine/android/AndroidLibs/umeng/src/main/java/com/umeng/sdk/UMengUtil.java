package com.umeng.sdk;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.text.TextUtils;
import android.util.Base64;

import androidx.annotation.NonNull;

import com.alibaba.fastjson2.JSON;
import com.bumptech.glide.Glide;
import com.tencent.tauth.Tencent;
import com.umeng.commonsdk.UMConfigure;
import com.umeng.socialize.PlatformConfig;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

import java.util.Map;
import java.util.function.Consumer;

public class UMengUtil {
    private UMengUtil() {
    }

    private static final class InstanceHolder {
        @SuppressLint("StaticFieldLeak")
        private static final UMengUtil mUtil = new UMengUtil();
    }

    public static UMengUtil getInstance() {
        return InstanceHolder.mUtil;
    }

    public interface UMListener {
        void onComplete(String data);
    }

    private Activity mContext = null;

    public void init(Activity context, String uAppId,
                     String wxAppId, String wxAppSecret,
                     String qqAppId, String qqAppSecret,
                     String sinaAppId, String sinaAppSecret) {
        mContext = context;

        UMConfigure.preInit(context, !TextUtils.isEmpty(uAppId) ? uAppId : "58004bfd67e58ee9b500395f", "channel");
        UMConfigure.init(context, !TextUtils.isEmpty(uAppId) ? uAppId : "58004bfd67e58ee9b500395f", "channel", UMConfigure.DEVICE_TYPE_PHONE, "");
        UMConfigure.setLogEnabled(false);
        UMConfigure.setEncryptEnabled(false);

        String fileProvider = String.format("%s.fileprovider", context.getPackageName());
        PlatformConfig.setFileProvider(fileProvider);

        if (!TextUtils.isEmpty(wxAppId) && !TextUtils.isEmpty(wxAppSecret))
            PlatformConfig.setWeixin(wxAppId, wxAppSecret);

        if (!TextUtils.isEmpty(qqAppId) && !TextUtils.isEmpty(qqAppSecret)) {
            Tencent.setIsPermissionGranted(true);
            PlatformConfig.setQQZone(qqAppId, qqAppSecret);
        }

        if (!TextUtils.isEmpty(sinaAppId) && !TextUtils.isEmpty(sinaAppSecret))
            PlatformConfig.setSinaWeibo(sinaAppId, sinaAppSecret, "http://sns.whalecloud.com");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (mContext == null) return;
        UMShareAPI.get(mContext).onActivityResult(requestCode, resultCode, data);
    }

    private UMListener mUMListener = null;

    // region 登录授权
    public void login(String share, UMListener umListener) {
        if (mContext == null) return;
        mUMListener = umListener;

        UMShareAPI.get(mContext).deleteOauth(mContext, getShareMedia(share), deleteOauth);
    }

    private final UMAuthListener getPlatformInfo = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onError(SHARE_MEDIA arg0, int arg1, Throwable arg2) {
            if (mUMListener != null) mUMListener.onComplete("");
        }

        @Override
        public void onComplete(SHARE_MEDIA arg0, int arg1, Map<String, String> arg2) {
            if (mUMListener != null) mUMListener.onComplete(JSON.toJSONString(arg2));
        }

        @Override
        public void onCancel(SHARE_MEDIA arg0, int arg1) {
            if (mUMListener != null) mUMListener.onComplete("");
        }
    };

    private final UMAuthListener deleteOauth = new UMAuthListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onError(SHARE_MEDIA arg0, int arg1, Throwable arg2) {
            isNeedAuthOnGetUserInfo(arg0);
        }

        @Override
        public void onComplete(SHARE_MEDIA arg0, int arg1, Map<String, String> arg2) {
            isNeedAuthOnGetUserInfo(arg0);
        }

        @Override
        public void onCancel(SHARE_MEDIA arg0, int arg1) {
            isNeedAuthOnGetUserInfo(arg0);
        }
    };

    private void isNeedAuthOnGetUserInfo(SHARE_MEDIA share_media) {
        UMShareAPI.get(mContext).setShareConfig(new UMShareConfig().isNeedAuthOnGetUserInfo(true));
        UMShareAPI.get(mContext).getPlatformInfo(mContext, share_media, getPlatformInfo);
    }
    // endregion

    // region 分享
    public void share(String share, int type,
                      String href, String title, String summary, String image,
                      UMListener umListener) {
        if (mContext == null) return;
        mUMListener = umListener;

        if (type == 0) {
            shareWithWeb(getShareMedia(share), title, summary, image, href);
        } else if (type == 1) {
            shareWithText(getShareMedia(share), summary);
        } else if (type == 2) {
            shareWithImage(getShareMedia(share), image);
        } else if (type == 4) {
            byte[] bytes = Base64.decode(image, Base64.DEFAULT);
            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            shareWithImage(getShareMedia(share), bitmap);
        } else {
            if (mUMListener != null) mUMListener.onComplete("");
        }
    }

    private final UMShareListener shareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            if (mUMListener != null) mUMListener.onComplete("1");
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            if (mUMListener != null) mUMListener.onComplete("");
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            if (mUMListener != null) mUMListener.onComplete("");
        }
    };

    private void shareWithWeb(final SHARE_MEDIA share_media, final String title, final String desc, final String thumb, final String web) {
        final ShareAction shareAction = new ShareAction(mContext);
        shareAction.setPlatform(share_media)
                .withMedia(createUMWeb(title, desc, thumb, web))
                .setCallback(shareListener)
                .share();
    }

    @NonNull
    private UMWeb createUMWeb(String title, String desc, String thumb, String web) {
        UMImage umThumb = TextUtils.isEmpty(thumb) ? null : new UMImage(mContext, thumb);
        UMWeb umWeb = new UMWeb(web);
        umWeb.setTitle(title);
        umWeb.setThumb(umThumb);
        umWeb.setDescription(desc);
        return umWeb;
    }

    private void shareWithText(final SHARE_MEDIA share_media, final String text) {
        final ShareAction shareAction = new ShareAction(mContext);
        shareAction.setPlatform(share_media)
                .withText(text)
                .setCallback(shareListener)
                .share();
    }

    private void shareWithImage(final SHARE_MEDIA share_media, final String img) {
        new Thread(() -> {
            try {
                final Bitmap resource = Glide.with(mContext.getBaseContext()).asBitmap().load(img).submit().get();
                if (resource != null) {
                    mContext.runOnUiThread(() -> shareWithImage(share_media, resource));
                } else {
                    if (mUMListener != null) mUMListener.onComplete("");
                }
            } catch (Exception e) {
                if (mUMListener != null) mUMListener.onComplete("");
            }
        }).start();
    }

    private void shareWithImage(final SHARE_MEDIA share_media, final Bitmap img) {
        final ShareAction shareAction = new ShareAction(mContext);
        shareAction.setPlatform(share_media)
                .withMedia(createUMImage(img))
                .setCallback(shareListener)
                .share();
    }

    @NonNull
    private UMImage createUMImage(Bitmap resource) {
        UMImage image = new UMImage(mContext, resource);
        Bitmap bitmap = ThumbnailUtils.extractThumbnail(resource, resource.getWidth() / 10, resource.getHeight() / 10);
        UMImage umThumb = new UMImage(mContext, bitmap);
        image.setThumb(umThumb);
        return image;
    }
    // endregion

    private SHARE_MEDIA getShareMedia(String share_media) {
        if (TextUtils.equals(share_media, "wx")) return SHARE_MEDIA.WEIXIN;
        if (TextUtils.equals(share_media, "qq")) return SHARE_MEDIA.QQ;
        if (TextUtils.equals(share_media, "sina")) return SHARE_MEDIA.SINA;
        else return SHARE_MEDIA.convertToEmun(share_media);
    }

    public void getOaid(Context context, Consumer<String> callback) {
        UMConfigure.getOaid(context, s -> {
            if (callback != null) callback.accept(s);
        });
    }
}
