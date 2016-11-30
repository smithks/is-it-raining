package com.smithkeegan.isitraining;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * @author Keegan Smith
 * @since 11/28/2016
 */

public class SettingsFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
