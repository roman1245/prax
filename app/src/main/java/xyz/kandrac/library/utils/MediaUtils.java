package xyz.kandrac.library.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FilenameFilter;

/**
 * Created by Jan Kandrac on 10.6.2016.
 */
public class MediaUtils {

    public static final String LOG_TAG = MediaUtils.class.getName();

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


    public static boolean checkCameraHardware(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
    }

    public static boolean delete(Context context, final String filePath) {
        File imageDirectory = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        if (imageDirectory == null) {
            return false;
        }

        File[] files = imageDirectory.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File file, String s) {
                return s.equals(filePath);
            }
        });

        if (files.length == 1) {
            if (files[0].delete()) {
                LogUtils.d(LOG_TAG, "file " + filePath + " deleted");
                return true;
            } else {
                LogUtils.d(LOG_TAG, "file " + filePath + " not deleted");
            }
        } else {
            LogUtils.d(LOG_TAG, "file " + filePath + " doesn't exists");
        }
        return false;
    }

}
