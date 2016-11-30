package com.smithkeegan.isitraining;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * @author Keegan Smith
 * @since 11/21/2016
 */
public class TodayForecastActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    public final static String LOG_TAG = "smithkeegan.isitraining";
    public final int LOCATION_REQUEST_CODE = 1;
    private GoogleApiClient mGoogleApiClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.today_forecast_activity);

        //Hide title in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        //Create googleApiClient
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this).addOnConnectionFailedListener(this).addApi(LocationServices.API).build();

        //If we do not have the location permission and we have not already asked the user for the location permission query them now
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            //  && !PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getResources().getString(R.string.requested_location_permission),false)) {

            //If we don't have permission we cannot use the device location
            PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getResources().getString(R.string.use_device_location),false).apply();
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_REQUEST_CODE);
        }

        if (savedInstanceState == null) {
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.add(R.id.today_forecast_activity_layout, new TodayForecastFragment());
            transaction.commit();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null){
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient != null){
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                }
                //Set user has been asked. Don't query again.
                PreferenceManager.getDefaultSharedPreferences(this).edit().putBoolean(getResources().getString(R.string.requested_location_permission),true).apply();
                break;
        }
    }

    /**
     * Uses GoogleApiClient to get the devices location if proper permissions are available.
     */
    private void fetchLocationFromAPI(){
        //Check for location permission before requesting location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.v(TodayForecastActivity.class.getSimpleName(),"Lat = "+ Double.toString(latitude) + " Lon = "+ Double.toString(longitude));

            String userLocation = "lat="+Double.toString(latitude)+"&lon="+Double.toString(longitude);
            //Indicate we want to use user location and set location coordinates.
            PreferenceManager.getDefaultSharedPreferences(this).edit()
                    .putString(getResources().getString(R.string.user_device_location_lat_long),userLocation)
                    .putBoolean(getResources().getString(R.string.use_device_location),true)
                    .apply();
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
