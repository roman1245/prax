package xyz.kandrac.library.utils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

/**
 * Created by Jan Kandrac on 10.6.2016.
 */
public class MediaUtils {


    public static String x(Intent data, Context context) {
        String result;
        Uri photoUri = data.getData();
        Cursor cursor = context.getContentResolver().query(photoUri, new String[]{MediaStore.Images.Media.DATA}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            cursor.moveToFirst();
            result = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            cursor.close();
        } else {
            return null;
        }
        return result;
    }
}
