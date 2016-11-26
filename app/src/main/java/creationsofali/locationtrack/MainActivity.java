package creationsofali.locationtrack;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    Button buttonStartStop;
    Button fabGotoMap;
    TextView tvLatitude;
    TextView tvLongitude;

    int buttonCode = 0;

    private BroadcastReceiver receiver;

    protected double latitude;
    protected double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        LocationService.context = this;

        buttonStartStop = (Button) findViewById(R.id.buttonStartStop);
        fabGotoMap = (Button) findViewById(R.id.fabGotoMap);

        tvLatitude = (TextView) findViewById(R.id.tvLatitude);
        tvLongitude = (TextView) findViewById(R.id.tvLongitude);

        if (!checkRuntimePermissions()) {
            // if permission granted or not required (for API < 23)
            enableButtons();
        }


    }


    private void enableButtons() {

        buttonStartStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (buttonCode == 0) {
                    startService(new Intent(getApplicationContext(), LocationService.class));
                    buttonStartStop.setText("stop");
                    buttonCode = 1;
                    Log.d("BUTTON START", "clicked.");

                } else { // if buttonCode = 1
                    stopService(new Intent(getApplicationContext(), LocationService.class));
                    buttonStartStop.setText("start");
                    buttonCode = 0;
                    Log.d("BUTTON STOP", "clicked.");

                }
            }
        });

    }

    private boolean checkRuntimePermissions() {
        // for API 23 or newer
        if (Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 10);

            // ask for permission
            return true;
        }
        // for API below (older than) 23 or permission already granted
        return false;  // then don't ask for permission
    }


    // handle user responses for permission checking, if needed
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 10) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableButtons(); // to start service
            } else {
                // ask for permission again
                checkRuntimePermissions();
            }
        }
    }

    // handling receiver
    @Override
    protected void onResume() {
        super.onResume();
        if (receiver == null) {
            receiver = new BroadcastReceiver() {
                @Override
                public void onReceive(final Context context, Intent intent) {

                    latitude = intent.getDoubleExtra("lat", 1);
                    longitude = intent.getDoubleExtra("lon", 2);
                    Log.d("LAT MAIN", latitude + "");
                    Log.d("LON MAIN", longitude + "");

                    tvLatitude.append(latitude + "\n");
                    tvLongitude.append(longitude + "\n");

                }
            };
            //fab listener
            fabGotoMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent maps = new Intent(MainActivity.this, MapsActivity.class)
                            .putExtra("lat", latitude)
                            .putExtra("lon", longitude);
                    startActivity(maps);
                }
            });

        }
        registerReceiver(receiver, new IntentFilter("locationUpdate"));
        Log.d("RECEIVER onResume", "receiver registered");
    }


    // avoid memory leaks
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (receiver != null) {
            unregisterReceiver(receiver);
            Log.d("RECEIVER onDestroy", "receiver stopped");
        }
    }
}
