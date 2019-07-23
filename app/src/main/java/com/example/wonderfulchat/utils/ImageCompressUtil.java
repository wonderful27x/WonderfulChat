package com.example.wonderfulchat.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class ImageCompressUtil {
    public static Bitmap decodeBitmapFromFile(String path, int reWith, int reHeight){
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        options.inSampleSize = calculateInSampleSize(options,reWith,reHeight);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(path,options);
        return bitmap;
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reWith, int reHeight){
        if(reWith == 0 || reHeight == 0){
            return 1;
        }
        final int with = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;
        if(height > reHeight || with > reWith){
            final int halfHeight = height/2;
            final int halfWith = with/2;
            while((halfHeight/inSampleSize)>=reHeight&&(halfWith/inSampleSize)>reWith){
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
