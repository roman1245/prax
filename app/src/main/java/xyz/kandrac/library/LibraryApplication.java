package xyz.kandrac.library;

import android.app.Application;
import android.content.Context;

import xyz.kandrac.library.dagger.component.ApplicationComponent;
import xyz.kandrac.library.dagger.component.DaggerApplicationComponent;
import xyz.kandrac.library.dagger.component.DaggerNetComponent;
import xyz.kandrac.library.dagger.component.NetComponent;
import xyz.kandrac.library.dagger.module.ApplicationModule;
import xyz.kandrac.library.dagger.module.NetModule;

/**
 * Base application class for generating Dagger 2 graphs and for initializing Crashlytics
 * <p>
 * Created by jan on 6.12.2016.
 */
public class LibraryApplication extends Application {

    private NetComponent mNetComponent;
    private ApplicationComponent mAppComponent;

    protected ApplicationModule getApplicationModule() {
        return new ApplicationModule(this);
    }

    protected NetModule getNetModule() {
        return new NetModule(BuildConfig.LIBRARY_API_URL);
    }

    public static ApplicationComponent getAppComponent(Context context) {
        LibraryApplication app = (LibraryApplication) context.getApplicationContext();
        if (app.mAppComponent == null) {
            app.mAppComponent = DaggerApplicationComponent.builder()
                    .applicationModule(app.getApplicationModule())
                    .build();
        }
        return app.mAppComponent;
    }

    public static NetComponent getNetComponent(Context context) {

        LibraryApplication app = (LibraryApplication) context.getApplicationContext();
        if (app.mNetComponent == null) {
            app.mNetComponent = DaggerNetComponent.builder()
                    .applicationModule(app.getApplicationModule())
                    .netModule(app.getNetModule())
                    .build();
        }
        return app.mNetComponent;
    }

}
