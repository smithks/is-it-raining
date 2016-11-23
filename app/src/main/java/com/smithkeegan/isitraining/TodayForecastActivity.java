package com.smithkeegan.isitraining;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

/**
 * @author Keegan Smith
 * @since 11/21/2016
 */
public class TodayForecastActivity extends AppCompatActivity {

    public final static String LOG_TAG = "smithkeegan.isitraining";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.today_forecast_activity);

        //Hide title in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.today_forecast_activity_layout,new TodayForecastFragment());
            transaction.commit();
        }
    }
}
