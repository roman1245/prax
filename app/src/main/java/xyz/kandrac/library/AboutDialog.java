package xyz.kandrac.library;

import android.app.DialogFragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * This is the dialog that should be displayed when user requests for About page.
 * It contains details about developer's intentions for this application. To see
 * content directly please see @layout/dialog_fragment_about where the main content is
 * defined in @string/about_text
 * <p/>
 * It will be visible the same way for each device since it should be only simple
 * dialog and it won't replace any fragment.
 * <p/>
 * Created by VizGhar on 19.10.2015.
 */
public class AboutDialog extends DialogFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_fragment_about, container, false);
    }
}
