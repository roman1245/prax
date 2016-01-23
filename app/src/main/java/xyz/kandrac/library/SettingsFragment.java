package xyz.kandrac.library;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

/**
 * Created by kandrac on 15/12/15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String KEY_PREF_NOTIFICATION_DAYS = "notification_days";
    public static final String KEY_PREF_LIBRARY_DEFAULT = "default_library";
    public static final String KEY_PREF_LIBRARY_ENABLED = "library_enabled";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setNotificationDays();
        setDefaultLibrary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_NOTIFICATION_DAYS)) {
            setNotificationDays();
        } else if (key.equals(KEY_PREF_LIBRARY_DEFAULT)) {
            setDefaultLibrary();
        } else if (key.equals(KEY_PREF_LIBRARY_ENABLED)) {
            ((MainActivity) getActivity()).checkLibrariesPreferences();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    private void setNotificationDays() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String notifyInDays = sharedPref.getString(SettingsFragment.KEY_PREF_NOTIFICATION_DAYS, "20");

        Preference notificationDaysPreference = findPreference(KEY_PREF_NOTIFICATION_DAYS);
        notificationDaysPreference.setSummary(getString(R.string.preferences_notifications_borrowed_set, notifyInDays));
    }

    private void setDefaultLibrary() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLibraryName = sharedPref.getString(SettingsFragment.KEY_PREF_LIBRARY_DEFAULT, "");

        Preference defaultLibraryPreference = findPreference(KEY_PREF_LIBRARY_DEFAULT);
        defaultLibraryPreference.setSummary(defaultLibraryName);
    }
}
