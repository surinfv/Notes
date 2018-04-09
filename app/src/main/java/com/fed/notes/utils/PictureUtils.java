package com.fed.notes.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;


public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, Activity activity) {
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }

    private static Bitmap getScaledBitmap(String path, int destWidth, int destHeight) {
        //чтение размеров картинки на диске
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float scrWidth = options.outWidth;
        float scrHeight = options.outHeight;

//        Log.e("LOOKscrWidth", "" + scrWidth);
//        Log.e("LOOKscrHeight", "" + scrHeight);
//        Log.e("LOOKdestWidth", "" + destWidth);
//        Log.e("LOOKdestHeight", "" + destHeight);

        //вычисление степени масштабирования
        int inSampleSize = 1;
        if (scrHeight > destHeight || scrWidth > destWidth) {
            if (scrWidth > scrHeight) {
                inSampleSize = Math.round(scrHeight / destHeight);
            } else {
                inSampleSize = Math.round(scrWidth / destWidth);
            }
        }
        inSampleSize *= 2;

//        Log.e("LOOKSampleSize", "" + inSampleSize);

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;
        options.inPreferredConfig = Bitmap.Config.RGB_565;

        //чтение данных и создание итгового изображения
        return BitmapFactory.decodeFile(path, options);
    }
}