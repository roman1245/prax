package xyz.kandrac.library;

import android.app.Application;

import xyz.kandrac.library.net.OkHttpConfigurator;

/**
 * Created by VizGhar on 2.11.2015.
 */
public class LibraryApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OkHttpConfigurator.getInstance().init();
    }
}
