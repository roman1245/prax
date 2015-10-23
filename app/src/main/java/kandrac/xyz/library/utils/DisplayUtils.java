package kandrac.xyz.library.utils;

import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;

import kandrac.xyz.library.R;

/**
 * Created by kandrac on 21/10/15.
 */
public final class DisplayUtils {

    private DisplayUtils(){}

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
}
