package xyz.kandrac.library.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import xyz.kandrac.library.MainActivity;
import xyz.kandrac.library.R;
import xyz.kandrac.library.flow.importwizard.ImportWizardActivity;

/**
 * Settings displayed based on {@link R.xml#preferences} (preferences.xml) file
 *
 * Created by kandrac on 15/12/15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String KEY_PREF_LIBRARY_DEFAULT = "default_library";
    public static final String KEY_PREF_LIBRARY_ENABLED = "library_enabled";
    public static final String KEY_PREF_CONSERVATIVE_ENABLED = "conservative_enabled";
    public static final String KEY_PREF_IMPORT = "preferences_import";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        setDefaultLibrary();

        findPreference(KEY_PREF_IMPORT).setOnPreferenceClickListener(this);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_LIBRARY_DEFAULT)) {
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

    private void setDefaultLibrary() {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String defaultLibraryName = sharedPref.getString(SettingsFragment.KEY_PREF_LIBRARY_DEFAULT, "");

        Preference defaultLibraryPreference = findPreference(KEY_PREF_LIBRARY_DEFAULT);
        defaultLibraryPreference.setSummary(defaultLibraryName);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (KEY_PREF_IMPORT.equals(preference.getKey())) {
            startActivity(new Intent(getActivity(), ImportWizardActivity.class));
        } else {
            return false;
        }
        return true;
    }
}
