package com.cocos.game;

import android.Manifest;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

public class FileUtil {
    private final static String DATE_PATTERN = "yyyyMMdd_HHmmss";
    private final static String MIME_TYPE_VALUE = "image/jpeg";
    private final static String SUFFIX = ".jpeg";
    private final static String DIR_COCOS = "CocosGame";
    private final static String DIR_DCIM = Environment.DIRECTORY_DCIM;

    private static Context context;
    private static Bitmap bitmap;

    private FileUtil() {
    }

    /**
     * @param context Context
     * @param model   Object
     */
    public static void saveImage(final @NonNull Context context, final Object model) {
        new Thread(() -> {
            try {
                Bitmap bitmap = Glide.with(context).asBitmap().load(model).submit().get();
                if (bitmap != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) saveImageQ(context, bitmap);
                    else saveBitmap(context, bitmap);
                } else {
                    toastShow(context, "图片下载失败！");
                }
            } catch (InterruptedException | ExecutionException e) {
                toastShow(context, "保存失败:Exception " + e.getMessage());
            }
        }).start();
    }

    /**
     * @param context Context
     * @param bitmap  Bitmap
     */
    public static void saveBitmap(Context context, Bitmap bitmap) {
        try {
            String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
            if (hasPermission(context, permissions)) {
                File file = createFile(context, DIR_DCIM, fileName(null, SUFFIX));
                FileOutputStream out = new FileOutputStream(file);
                // 格式为 JPEG，照相机拍出的图片为JPEG格式的，PNG格式的不能显示在相册中
                if (bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)) {
                    out.flush();
                    out.close();
                    // 插入图库
                    MediaStore.Images.Media.insertImage(context.getContentResolver(),
                            file.getAbsolutePath(), file.getName(), null);
                    toastShow(context, "保存成功:" + file.getAbsolutePath());
                    notifyGallery(context, file);
                }
            } else {
                FileUtil.context = context;
                FileUtil.bitmap = bitmap;
                ActivityCompat.requestPermissions((Activity) context, permissions, 0x100);
            }
        } catch (FileNotFoundException e) {
            toastShow(context, "保存失败 FileNotFound " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            toastShow(context, "保存失败 FileIO " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            toastShow(context, "保存失败 " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * @param context Context
     * @param bitmap  Bitmap
     */
    public static void saveImageQ(Context context, Bitmap bitmap) {
        try {
            File file = createFile(context, DIR_DCIM, fileName(null, SUFFIX));
            // 设置保存参数到ContentValues中
            ContentValues contentValues = new ContentValues();
            // 设置文件名
            contentValues.put(MediaStore.Images.Media.DISPLAY_NAME, file.getName());
            // android Q中不再使用DATA字段，而用RELATIVE_PATH代替
            // RELATIVE_PATH是相对路径不是绝对路径
            // DCIM是系统文件夹，关于系统文件夹可以到系统自带的文件管理器中查看，不可以写没存在的名字
            contentValues.put(MediaStore.Images.Media.RELATIVE_PATH, String.format("%s/%s", DIR_DCIM, DIR_COCOS));
            // 设置文件类型
            contentValues.put(MediaStore.Images.Media.MIME_TYPE, MIME_TYPE_VALUE);
            // 执行insert操作，向系统文件夹中添加文件
            // EXTERNAL_CONTENT_URI代表外部存储器，该值不变
            Uri uri = context.getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
            if (uri != null) {
                // 若生成了uri，则表示该文件添加成功
                // 使用流将内容写入该uri中即可
                OutputStream outputStream = context.getContentResolver().openOutputStream(uri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream);
                    outputStream.flush();
                    outputStream.close();

                    toastShow(context, "保存成功:" + file.getAbsolutePath());
                    context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri));
                } else {
                    toastShow(context, "保存失败 FileStream is null");
                }
            } else {
                toastShow(context, "保存失败 FileUri is null");
            }
        } catch (Exception e) {
            toastShow(context, "保存失败");
        }
    }

    public static void onRequestPermissionsResult(int requestCode, int[] grantResults) {
        if (requestCode == 0x100) {
            List<Integer> list = new ArrayList<>();
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) list.add(grantResult);
            }
            if (list.isEmpty()) {
                saveBitmap(context, bitmap);
            }
        }
    }

    private static boolean hasPermission(Context context, String[] array) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        List<String> list = new ArrayList<>();
        for (String s : array) {
            if (ContextCompat.checkSelfPermission(context, s) != PackageManager.PERMISSION_GRANTED) {
                list.add(s);
            }
        }
        return list.isEmpty();
    }

    /**
     * View to Bitmap
     *
     * @param view View
     * @return Bitmap
     */
    public static Bitmap viewToBitmap(View view) {
        // ARGB_8888：四个通道都是8位，每个像素占用4个字节，图片质量是最高的，但是占用的内存也是最大的；
        // ARGB_4444：四个通道都是4位，每个像素占用2个字节，图片的失真比较严重；
        // RGB_565：没有A通道，每个像素占用2个字节，图片失真小，但是没有透明度；
        // ALPHA_8：只有A通道，每个像素占用1个字节大大小，只有透明度，没有颜色值。
        Bitmap shortcut = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(shortcut);
        view.draw(canvas);
        return shortcut;
    }

    /**
     * @param context  Context
     * @param type     {@link Environment#DIRECTORY_DCIM}
     * @param fileName String
     * @return File
     */
    public static File createFile(Context context, String type, String fileName) {
        File storageFile = new File(createExternalDir(context, type), fileName);
        if (storageFile.exists()) {
            // noinspection ResultOfMethodCallIgnored
            storageFile.delete();
        }
        // noinspection ResultOfMethodCallIgnored
        storageFile.setWritable(true);
        return storageFile;
    }

    /**
     * @param context Context
     * @param type    {@link Environment#DIRECTORY_DCIM}
     */
    public static File createExternalDir(Context context, String type) {
        File storageDir;
        // 判断是否有SD卡,优先使用SD卡存储,当没有SD卡时使用手机存储
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            storageDir = new File(Environment.getExternalStoragePublicDirectory(type), DIR_COCOS);
        } else {
            storageDir = new File(context.getExternalFilesDir(type), DIR_COCOS);
        }
        if (!storageDir.exists()) {
            // noinspection ResultOfMethodCallIgnored
            storageDir.mkdirs();
        }
        return storageDir;
    }

    /**
     * @param prefix String 前缀[文件名]
     * @param suffix String 后缀[扩展名]
     * @return fileName String start_yyyyMMdd_HHmmss_end
     */
    public static String fileName(String prefix, String suffix) {
        suffix = MimeTypeMap.getFileExtensionFromUrl(suffix);
        if (TextUtils.isEmpty(suffix)) {
            suffix = MimeTypeMap.getFileExtensionFromUrl(prefix);
            if (!TextUtils.isEmpty(suffix)) {
                prefix = prefix.substring(0, prefix.length() - suffix.length() - 1);
            }
        }
        return (TextUtils.isEmpty(prefix) ? "" : (prefix + "_"))
                + DateTimeFormatter.ofPattern(DATE_PATTERN, Locale.getDefault()).format(LocalDateTime.now())
                + (TextUtils.isEmpty(suffix) ? "" : ("." + suffix));
    }

    /**
     * @param context Context
     * @param file    File
     */
    private static void notifyGallery(final Context context, final File file) {
        Uri uri = Uri.fromFile(file);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, uri);
        context.sendBroadcast(intent);
    }

    private static void toastShow(final Context context, final String msg) {
        if (context instanceof Activity activity) {
            activity.runOnUiThread(() -> Toast.makeText(context, msg, Toast.LENGTH_LONG).show());
        }
    }
}
