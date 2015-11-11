package kandrac.xyz.library;

import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;

/**
 * Extend this class in order to make {@code Fragment} with its own title. Title is always defined
 * as {@link StringRes} so that it can be obtained with no need to work with {@code Context} which
 * might not be initialised for this {@code Fragment} yet.
 * <p>
 * The {@code Fragment} can be used well with other {@code SubtitledFragment}s in same
 * {@link android.app.Activity} where {@code Activity} is asking those fragments on their titles
 * and displays the titles.
 * <p>
 * It is recommended to display those titles in {@code ActionBar} right after {@code Fragment} is
 * added or replaces other {@code Fragment}. Title should be visible only with its {@code Fragment}
 * <p>
 * Created by kandrac on 09/11/15.
 */
public abstract class SubtitledFragment extends Fragment {

    /**
     * Get title of this {@code Fragment}. This title can be used as text visible to user of
     * the application while {@code Fragment} is visible.
     *
     * @return {@link StringRes} of title
     */
    @StringRes
    public abstract int getTitle();
}
