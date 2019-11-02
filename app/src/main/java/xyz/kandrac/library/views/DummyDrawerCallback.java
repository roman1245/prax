package xyz.kandrac.library.views;

import androidx.drawerlayout.widget.DrawerLayout;
import android.view.View;

/**
 * You have to override only {@link DrawerLayout.DrawerListener#onDrawerClosed(View)} method to
 * specify close behavior. Other methods will do nothing if not overridden.
 *
 * Created by kandrac on 09/11/15.
 */
public class DummyDrawerCallback implements DrawerLayout.DrawerListener {

    @Override
    public void onDrawerClosed(View drawerView) {

    }

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
