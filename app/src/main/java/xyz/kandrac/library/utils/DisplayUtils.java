package xyz.kandrac.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import xyz.kandrac.library.R;

/**
 * Created by kandrac on 21/10/15.
 */
public final class DisplayUtils {

    public static final String LOG_TAG = "DisplayUtils";

    private DisplayUtils() {
    }

    public static void displayScaledImage(Context context, String imageFileName, ImageView imageView) {
        int densityDpi = getPixelsFromDips(96, context);
        displayScaledImage(context, imageFileName, imageView, densityDpi, densityDpi);
    }


    public static void displayScaledImage(Context context, String imageFileName, ImageView imageView, int width, int height) {
        if (imageFileName != null) {
            File f = new File(imageFileName);

            if (f.exists()) {
                Picasso.with(context).load(f).resize(width, height).centerCrop().into(imageView);
                return;
            }
        }

        imageView.setImageResource(R.drawable.ic_book_white);
    }

    public static int getPixelsFromDips(int dips, Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return (int) (metrics.density * dips);
    }

    public static void resizeImageFile(File imageFile, int width, int quality) {

        int orientation = ExifInterface.ORIENTATION_NORMAL;
        try {
            ExifInterface exif = new ExifInterface(imageFile.getAbsolutePath());
            orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
        } catch (IOException ex) {
            LogUtils.d(LOG_TAG, "Error getting EXIF from file. Assuming orientation is normal");
        }

        LogUtils.d(LOG_TAG, "Resizing " + imageFile.getName() + " with size of " + imageFile.length() / 1000 + "kB");

        Bitmap b = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        if (b == null) {
            LogUtils.w(LOG_TAG, "Cannot decode file to bitmap");
            return;
        }

        float ratio = (float) b.getWidth() / (float) b.getHeight();
        Bitmap out = getResizedBitmap(b, width, (int) ((float) width / ratio), orientation);

        File file = new File(imageFile.getParent(), "temp.jpg");

        FileOutputStream fOut;

        try {
            fOut = new FileOutputStream(file);
            out.compress(Bitmap.CompressFormat.JPEG, quality, fOut);
            fOut.flush();
            fOut.close();
            b.recycle();
            out.recycle();
        } catch (Exception e) {
            LogUtils.w(LOG_TAG, "Error occurs while scaling image", e);
            return;
        }

        boolean renamed = file.renameTo(imageFile);

        if (renamed) {
            LogUtils.d(LOG_TAG, "File resized to size of " + imageFile.length() / 1000 + "kB");
        } else {
            if (file.delete()) {
                LogUtils.d(LOG_TAG, "Unable to store resized image");
            } else {
                LogUtils.d(LOG_TAG, "Unable to delete resized image");
            }
        }
    }

    /**
     * Get properly resized bitmap based on original bitmap, requested sizes and EXIF orientation
     *
     * @param original    original bitmap
     * @param newWidth    width of result bitmap
     * @param newHeight   height of result bitmap
     * @param orientation from EXIF details
     * @return resized bitmap
     */
    public static Bitmap getResizedBitmap(Bitmap original, int newWidth, int newHeight, int orientation) {
        int width = original.getWidth();
        int height = original.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        Matrix matrix = new Matrix();
        float rotate = 0;

        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                LogUtils.d(LOG_TAG, "Normal orientation");
                rotate = 0;
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                LogUtils.d(LOG_TAG, "Rotating by 180");
                rotate = 180;
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                LogUtils.d(LOG_TAG, "Rotating by 90");
                rotate = 90;
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                LogUtils.d(LOG_TAG, "Rotating by 270");
                rotate = 270;
                break;
        }

        matrix.postRotate(rotate);
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap resizedBitmap = Bitmap.createBitmap(original, 0, 0, width, height, matrix, false);
        original.recycle();
        return resizedBitmap;
    }
}
