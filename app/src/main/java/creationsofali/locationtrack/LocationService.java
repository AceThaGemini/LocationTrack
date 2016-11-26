package creationsofali.locationtrack;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;


/**
 * Created by ali on 11/4/16.
 */

@SuppressWarnings("MissingPermission")
public class LocationService extends Service {

    private LocationListener locationListener;
    private LocationManager locationManager;
    static Context context;

    private static long MIN_UPDATE_TIME = 5000; // in milliseconds
    private static long MIN_UPDATE_DISTANCE = 5; // in meters

    private boolean isGPSEnabled = false;
    private boolean isNetEnabled = false;


    // must override this
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // when service is started
    @Override
    public void onCreate() {

        Log.d("SERVICE onCreate", "service started");

        // initialize locationListener
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Intent locationIntent = new Intent("locationUpdate");
                double lat = location.getLatitude();
                double lon = location.getLongitude();

                Log.d("LAT VALUE", lat + "");
                Log.d("LON VALUE", lon + "");
                locationIntent.putExtra("lat", lat);
                locationIntent.putExtra("lon", lon);

                sendBroadcast(locationIntent);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                showSettingsAlert();
            }
        };

        // also initialize locationManager
        locationManager = (LocationManager) getApplicationContext()
                .getSystemService(Context.LOCATION_SERVICE);

        isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        isNetEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

        if (isNetEnabled) {
            // using network provider, high accuracy
            try {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_UPDATE_TIME,
                        MIN_UPDATE_DISTANCE,
                        locationListener);
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (isGPSEnabled) {
            // using gps provider (device only), low accuracy
            try {
                //noinspection MissingPermission
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_UPDATE_TIME,
                        MIN_UPDATE_DISTANCE,
                        locationListener);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        } else {
            // if both providers ain't all active
            locationListener.onProviderDisabled(LOCATION_SERVICE);
        }
    }

    // from onProviderDisabled()
    private void showSettingsAlert() {
        // if location services disabled
        AlertDialog.Builder dialog = new AlertDialog.Builder(context);
        // dialog title
        dialog.setTitle("Location Settings")
                // dialog message
                .setMessage("Locations service is not enabled. Would you like to change the settings?")
                .setPositiveButton("SETTINGS", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // take me to location settings
                        Intent gotoSettings = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                                // to start another activity from a service or fragment
                                .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                        startActivity(gotoSettings);
                    }
                })
                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // dismiss dialog
                        dialogInterface.dismiss();
                    }
                }).show();
    }

    // avoid memory leaks
    @Override
    public void onDestroy() {
        super.onDestroy();

        if (locationManager != null) {
            //noinspection MissingPermission
            locationManager.removeUpdates(locationListener);
            Log.d("SERVICE onDestroy", "service stopped");
        }
    }
}
