package com.fed.notes.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;

/**
 * Created by f on 16.05.2017.
 */

public class PictureUtils {

    public static Bitmap getScaledBitmap(String path, int destWidth, int destHeight){
        //чтение размеров картинки на диске
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, options);

        float scrWidth = options.outWidth;
        float scrHeigth = options.outHeight;

        //вычисление степени масштабирования
        int inSampleSize = 1;
        if (scrHeigth > destHeight || scrWidth > destWidth){
            if (scrWidth > scrHeigth){
                inSampleSize = Math.round(scrHeigth / destHeight);
            } else {
                inSampleSize = Math.round(scrWidth / destWidth);
            }
        }

        options = new BitmapFactory.Options();
        options.inSampleSize = inSampleSize;

        //чтение данных и создание итгового изображения
        return BitmapFactory.decodeFile(path, options);
    }

    public static Bitmap getScaledBitmap(String path, Activity activity){
        Point size = new Point();
        activity.getWindowManager().getDefaultDisplay().getSize(size);
        return getScaledBitmap(path, size.x, size.y);
    }
}