package com.smithkeegan.isitraining;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * @author Keegan Smith
 * @since 11/21/2016
 */
public class TodayForecastActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    public final static String LOG_TAG = "smithkeegan.isitraining";
    public final static String FRAGMENT_TAG_TODAY_FORECAST = "FRAGMENT_TAG_TODAY_FORECAST";
    public final int LOCATION_REQUEST_CODE = 1;
    private GoogleApiClient mGoogleApiClient;

    private final String LOCATION_LOADED_STATE_KEY = "LOCATION_LOADED_STATE_KEY";
    private boolean mLocationLoaded;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.today_forecast_activity);

        //Hide title in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //Clear the previous location loaded status
        //PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getResources().getString(R.string.user_device_location_loaded),false).apply();

        //Create googleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        //If we do not have the location permission request it now
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestLocationPermission();
        }
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        if (savedInstanceState.containsKey(LOCATION_LOADED_STATE_KEY)){
            mLocationLoaded = savedInstanceState.getBoolean(LOCATION_LOADED_STATE_KEY);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(LOCATION_LOADED_STATE_KEY,mLocationLoaded);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        checkCurrentActivityState();
    }

    /**
     * Based on permissions and the current activity state, launch the forecast fragment. Called each time the
     * activity is resumed and after each initial set up step.
     * Request User Permission -> Fetch Location -> Display Fragment
     */
    private void checkCurrentActivityState(){
        //Can't do anything without the appropriate permissions
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //If we have the appropriate permissions but do not have the location then load the location.
            if (!mLocationLoaded){
                mGoogleApiClient.connect();
            }
            //If the fragment is not already displayed and the device location has been found then display the fragment.
            else if (getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODAY_FORECAST) == null && mLocationLoaded){
                findViewById(R.id.permissions_required_layout).setVisibility(View.GONE); //Hide the permissions required layout
                displayTodayForecastFragment();
            }
        }else { //If we do not have permission then remove fragments if displayed.
            Fragment todayForecast = getSupportFragmentManager().findFragmentByTag(FRAGMENT_TAG_TODAY_FORECAST);

            if (todayForecast != null){
                getSupportFragmentManager().beginTransaction().remove(todayForecast).commit();
            }
        }
    }

    /**
     * Creates a new forecast fragment and adds it to the parent view of this activity.
     */
    private void displayTodayForecastFragment() {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(R.id.today_forecast_activity_layout, new TodayForecastFragment(), FRAGMENT_TAG_TODAY_FORECAST);
        transaction.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Handles the result from the request permissions dialog. Displays an explanation if the user refuses permissions.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (permissions.length == 0) { //No permissions to check
            return;
        }
        switch (requestCode) {
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) { //Permission granted.

                } else { //User denied permission, show explanation.
                    TextView permissionsText = ((TextView) findViewById(R.id.permissions_required_text_view));
                    Button permissionsButton = (Button) findViewById(R.id.permissions_required_allow_button);

                    findViewById(R.id.permissions_required_layout).setVisibility(View.VISIBLE);

                    permissionsText.setText(getResources().getString(R.string.permissions_required_string));

                    //Set the listener for the allow permissions button
                    permissionsButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            requestLocationPermission();
                        }
                    });

                    if (Build.VERSION.SDK_INT >= 23) { //Only available on devices running android version above 22
                        if (!shouldShowRequestPermissionRationale(permissions[0])) { //User checked "Never show again" have the allow button send them to app settings
                            permissionsText.setText(getResources().getString(R.string.permissions_required_string_long));
                            permissionsButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View view) {
                                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                                    intent.setData(uri);
                                    startActivity(intent);
                                }
                            });
                        }
                    }
                }
                break;
        }
    }

    /**
     * Launch the permission request dialog for location service.
     */
    private void requestLocationPermission() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getResources().getString(R.string.use_device_location), false).apply();
        ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
    }

    /**
     * Uses GoogleApiClient to get the devices location if proper permissions are available.
     */
    private void fetchLocationFromAPI() {
        //Check for location permission before requesting location
        if (mGoogleApiClient != null && ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            //Exit if no location was found
            if (location == null){
                return;
            }

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.v(TodayForecastActivity.class.getSimpleName(), "Lat = " + Double.toString(latitude) + " Lon = " + Double.toString(longitude));

            String userLocation = "lat=" + Double.toString(latitude) + "&lon=" + Double.toString(longitude);
            //Indicate we want to use user location and set location coordinates.
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getResources().getString(R.string.user_device_location_lat_long), userLocation)
                    .putBoolean(getResources().getString(R.string.use_device_location), true)
                    .apply();

            mLocationLoaded = true;
            checkCurrentActivityState();
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        fetchLocationFromAPI();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
