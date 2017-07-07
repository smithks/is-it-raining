package com.isitraining.keegansmith.is_it_pouring_refactor;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Parent activity for settings preference fragment.
 * @author Keegan Smith
 * @since 1/23/2017
 */

public class SettingsActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.settings_activity);
        getFragmentManager().beginTransaction().add(R.id.settings_layout,new SettingsFragment()).commit();
    }
}
