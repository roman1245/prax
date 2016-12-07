package xyz.kandrac.library.dagger.module;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import xyz.kandrac.library.utils.SharedPreferencesManager;

/**
 * Base Application module, that only provides {@link Application} and {@link SharedPreferences} for
 * other components.
 * <p>
 * Created by jan on 5.10.2016.
 */
@Module
public final class ApplicationModule {

    private Application mApplication;

    public ApplicationModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    SharedPreferences provideSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(mApplication);
    }

    @Provides
    @Singleton
    SharedPreferencesManager provideSharedPreferenceManager(SharedPreferences sharedPreferences) {
        return new SharedPreferencesManager(sharedPreferences);
    }
}
