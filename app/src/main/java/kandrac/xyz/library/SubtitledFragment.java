package kandrac.xyz.library;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

/**
 * Created by kandrac on 09/11/15.
 */
public abstract class SubtitledFragment extends Fragment {

    @StringRes
    public abstract int getTitle();
}
