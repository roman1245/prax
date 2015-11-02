package kandrac.xyz.library;

import android.app.Application;

import kandrac.xyz.library.net.OkHttpConfigurator;

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
