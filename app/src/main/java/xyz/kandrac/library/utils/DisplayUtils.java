package xyz.kandrac.library.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;

import xyz.kandrac.library.R;

/**
 * Created by kandrac on 21/10/15.
 */
public final class DisplayUtils {

    public static final String LOG_TAG = "DisplayUtils";

    private DisplayUtils() {
    }

    public static void displayScaledImage(Context context, String imageFileName, ImageView imageView) {
        if (imageFileName != null) {
            File f = new File(imageFileName);

            if (f.exists()) {
                int densityDpi = getPixelsFromDips(96, context);
                Picasso.with(context).load(f).resize(densityDpi, densityDpi).centerInside().into(imageView);
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

        LogUtils.d(LOG_TAG, "Resizing " + imageFile.getName() + " with size of " + imageFile.length() / 1000 + "kB");

        Bitmap b = BitmapFactory.decodeFile(imageFile.getAbsolutePath());

        if (b == null) {
            LogUtils.w(LOG_TAG, "Cannot decode file to bitmap");
            return;
        }

        float ratio = (float) b.getWidth() / (float) b.getHeight();
        Bitmap out = Bitmap.createScaledBitmap(b, width, (int) ((float) width / ratio), false);

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
}
