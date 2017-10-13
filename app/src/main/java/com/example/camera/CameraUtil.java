package com.example.camera;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by bear on 2017.10.12
 */

public class CameraUtil {
    private static final int FREE_SPACE = 20 * 1024 * 1024; //20M


    /**
     * 判断当前手机系统是否有默认的相机应用
     *
     * @param context
     * @return
     */
    public static boolean hasCameraApp(Context context) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
        return infos.size() > 0;
    }

    /**
     * 获取image的保存目录
     *
     * @param context
     * @return
     */
    public static String getPicDir(Context context) {
        String picDir = null;

        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)
                && context.getExternalCacheDir().getFreeSpace() > FREE_SPACE) {

            picDir = context.getExternalCacheDir().getPath();

        } else if (context.getCacheDir().getFreeSpace() > FREE_SPACE) {

            picDir = context.getCacheDir().getPath();

        }

        return picDir;
    }

    /**
     * 获取image的Uri
     *
     * @param context
     * @param picName
     * @return
     */
    public static Uri getPicUri(Context context, String picDir, String picName) {
        Uri picUri;

        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {

            picUri = Uri.fromFile(new File(picDir, picName));

        } else {

            try {
                picUri = FileProvider.getUriForFile(
                        context,
                        context.getPackageName() + ".file_provider",
                        new File(picDir, picName));
            } catch (Exception e) {
                return null;
            }

        }

        return picUri;
    }

    public static Intent getCameraIntent(Context context, Uri picUri) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, picUri);
        return intent;
    }

    public static Bitmap decodeImage(String picPath, int requestHeight, int requestWidth) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(picPath, options);

        int imageHeight = 0;
        int imageWidth = 0;

        if (bitmap == null || options.outHeight == -1 || options.outWidth == -1) {

            try {

                ExifInterface exif = new ExifInterface(picPath);
                imageHeight = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0);
                imageWidth = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0);

            } catch (IOException e) {
                e.printStackTrace();
            }

        } else {

            imageHeight = options.outHeight;
            imageWidth = options.outWidth;

        }

        options.inSampleSize = calculateSampleSize(imageHeight, imageWidth, requestHeight, requestWidth);
        options.inJustDecodeBounds = false;

        return BitmapFactory.decodeFile(picPath, options);
    }

    public static int calculateSampleSize(int imageHeight, int imageWidth, int height, int width) {
        int sampleSize = 1;

        while (imageHeight / sampleSize > height || imageWidth / sampleSize > width) {
            sampleSize *= 2;
        }

        return sampleSize;
    }

    /**
     * 返回拍摄照片时的旋转角度
     *
     * @return
     */
    public static int getImageRotation(String picPath) {
        try {

            ExifInterface exif = new ExifInterface(picPath);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            int degree = 0;
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }

            return degree;

        } catch (IOException e) {

            e.printStackTrace();
            return 0;

        }
    }
}
