package com.isitraining.keegansmith.is_it_pouring_refactor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
/**
 * Preference fragment for handling display and manipulation of preferences.
 * @author Keegan Smith
 * @since 1/23/2017
 */

public class SettingsFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener
{
    SharedPreferences mSharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);
        mSharedPreferences = getPreferenceScreen().getSharedPreferences();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);
        //Display current preference value under the preference.
        for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++){
            Preference preference = getPreferenceScreen().getPreference(i);
            updatePreference(preference);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Preference preference = findPreference(key);
        updatePreference(preference);
    }

    /**
     * Updates the summary line of the passed in preference.
     * @param preference the preference to update
     */
    public void updatePreference(Preference preference){
        if (preference instanceof ListPreference){
            preference.setSummary(((ListPreference) preference).getEntry());
        }
    }
}
