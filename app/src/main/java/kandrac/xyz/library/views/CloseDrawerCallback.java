package kandrac.xyz.library.views;

import android.support.v4.widget.DrawerLayout;
import android.view.View;

/**
 * You have to override only {@link DrawerLayout.DrawerListener#onDrawerClosed(View)} method to
 * specify close behavior. Other methods will do nothing if not overridden.
 *
 * Created by kandrac on 09/11/15.
 */
public abstract class CloseDrawerCallback implements DrawerLayout.DrawerListener {
    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {

    }

    @Override
    public void onDrawerOpened(View drawerView) {

    }

    @Override
    public void onDrawerStateChanged(int newState) {

    }
}
