package xyz.kandrac.library.utils;

import android.content.SharedPreferences;
import android.support.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Managing all application {@link SharedPreferences} from here. All names and
 * related operations should be obtained from here.
 * <p>
 * Created by jan on 28.10.2016.
 */
@SuppressWarnings("unused")
public class SharedPreferencesManager {

    public static final String KEY_PREF_LIBRARY_ENABLED = "library_enabled";

    // Define the list of accepted constants and declare the NavigationMode annotation
    @Retention(RetentionPolicy.SOURCE)
    @StringDef({KEY_PREF_LIBRARY_ENABLED})
    @interface ApplicationPreferenceKey {
    }

    private SharedPreferences mPreferences;

    public SharedPreferencesManager(SharedPreferences sharedPreferences) {
        mPreferences = sharedPreferences;
    }

    public void editPreference(@ApplicationPreferenceKey String preferenceKey, boolean newPreferenceValue) {
        mPreferences.edit().putBoolean(preferenceKey, newPreferenceValue).apply();
    }

    public void editPreference(@ApplicationPreferenceKey String preferenceKey, int newPreferenceValue) {
        mPreferences.edit().putInt(preferenceKey, newPreferenceValue).apply();
    }

    public void editPreference(@ApplicationPreferenceKey String preferenceKey, long newPreferenceValue) {
        mPreferences.edit().putLong(preferenceKey, newPreferenceValue).apply();
    }

    public void editPreference(@ApplicationPreferenceKey String preferenceKey, String newPreferenceValue) {
        mPreferences.edit().putString(preferenceKey, newPreferenceValue).apply();
    }

    public void editPreference(@ApplicationPreferenceKey String preferenceKey, float newPreferenceValue) {
        mPreferences.edit().putFloat(preferenceKey, newPreferenceValue).apply();
    }

    public boolean getBooleanPreference(@ApplicationPreferenceKey String preferenceKey, boolean defaultValue) {
        return mPreferences.getBoolean(preferenceKey, defaultValue);
    }

    public boolean getBooleanPreference(@ApplicationPreferenceKey String preferenceKey) {
        return mPreferences.getBoolean(preferenceKey, false);
    }

    public String getStringPreference(@ApplicationPreferenceKey String preferenceKey) {
        return mPreferences.getString(preferenceKey, null);
    }
}
