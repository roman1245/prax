package xyz.kandrac.library.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import xyz.kandrac.library.R;
import xyz.kandrac.library.mvp.view.MainActivity;
import xyz.kandrac.library.utils.SharedPreferencesManager;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;

/**
 * Settings displayed based on {@link R.xml#preferences} (preferences.xml) file
 * <p>
 * Created by kandrac on 15/12/15.
 */
public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener, Preference.OnPreferenceClickListener {

    public static final String KEY_PREF_LIBRARY_DEFAULT = "default_library";
    public static final String KEY_PREF_LIBRARY_ENABLED = "library_enabled";
    public static final String KEY_PREF_CONSERVATIVE_ENABLED = "conservative_enabled";
    public static final String KEY_PREF_SK_CZ_ENABLED = "search_sk_cz";
    public static final String KEY_PREF_PRIVACY = "privacy_policy";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load the preferences from an XML resource
        addPreferencesFromResource(R.xml.preferences);

        findPreference(KEY_PREF_PRIVACY).setOnPreferenceClickListener(this);

        setDefaultLibrary();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {
        if (key.equals(KEY_PREF_LIBRARY_DEFAULT)) {
            setDefaultLibrary();
        } else if (key.equals(KEY_PREF_LIBRARY_ENABLED)) {
            boolean enable = PreferenceManager
                    .getDefaultSharedPreferences(getActivity())
                    .getBoolean(SharedPreferencesManager.KEY_PREF_LIBRARY_ENABLED, true);

            ((MainActivity) getActivity()).setLibraryItemVisibility(enable);
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
        SharedPreferences sharedPref = getDefaultSharedPreferences(getActivity());
        String defaultLibraryName = sharedPref.getString(SettingsFragment.KEY_PREF_LIBRARY_DEFAULT, "");

        Preference defaultLibraryPreference = findPreference(KEY_PREF_LIBRARY_DEFAULT);
        defaultLibraryPreference.setSummary(defaultLibraryName);
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        switch (preference.getKey()) {
            case KEY_PREF_PRIVACY:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://kandrac.xyz/library-privacy-policy/"));
                startActivity(browserIntent);
                return true;
        }
        return false;
    }
}
