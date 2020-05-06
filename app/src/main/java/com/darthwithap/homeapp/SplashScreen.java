package com.darthwithap.homeapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Objects;


public class SplashScreen extends AppCompatActivity{

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest.Builder builder;
    private boolean isLocationAllowed;
    private static final int REQUEST_CHECK_SETTINGS = 102;
    private static final String LOG_TAG = "CheckNetworkStatus";
    private NetworkChangeReceiver receiver;
    private boolean isConnected = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        IntentFilter filter = new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION);
        receiver = new NetworkChangeReceiver();
        registerReceiver(receiver, filter);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    Toast.makeText(SplashScreen.this, "Location not Allowed", Toast.LENGTH_SHORT).show();
                    isLocationAllowed = false;
                    Log.e("LocationStatus",Boolean.toString(isLocationAllowed));
                }
                else {
                    isLocationAllowed = true;
                    for (Location location : locationResult.getLocations()) {

                    }
                }
            }
        };

        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);


    }


    private void checkLocationSetting(LocationSettingsRequest.Builder builder) {
        SettingsClient client = LocationServices.getSettingsClient(this);
        final Task<LocationSettingsResponse> task = client.checkLocationSettings(builder.build());

        task.addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                startLocationUpdates();
                return;
            }
        });

        task.addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull final Exception e) {
                if (e instanceof ResolvableApiException) {
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(SplashScreen.this);
                    builder1.setMessage("Continuous Location Request");
                    builder1.setTitle("Location Request");
                    builder1.create();
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            try {
                                resolvableApiException.startResolutionForResult(SplashScreen.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(SplashScreen.this, "Location Update Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder1.show();
                }
            }
        });
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123: {
                if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    showPermissionAlert();
                } else {
                    if (ActivityCompat.checkSelfPermission((getApplicationContext()), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                            && ActivityCompat.checkSelfPermission((getApplicationContext()), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                        fetchLastLocation();
                    }
                }
            }
        }
    }

    private void fetchLastLocation() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //Handle case where user grants permission
                showPermissionAlert();
                return;
            }
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            Log.e("Last Location: ", location.toString());
                        }
                    }
                });
    }

    private void showPermissionAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startLocationUpdates();
            } else checkLocationSetting(builder);
        }
    }

    public void startLocationUpdates() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, null);
    }

    public void splash() {

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (SavedSharedPreference.getUserId(getApplicationContext()).length()==0) {
                    Intent intent = new Intent(getApplicationContext(),
                            MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                else {
                    if (SavedSharedPreference.getCustomerType(getApplicationContext()).equals("Customer")) {
                        Intent intent = new Intent(getApplicationContext(),
                                NavTech.class);
                        startActivity(intent);
                        finish();
                    }
                    else if (SavedSharedPreference.getCustomerType(getApplicationContext()).equals("Technician")) {
                        Intent intent = new Intent(getApplicationContext(),
                                NavCust.class);
                        startActivity(intent);
                        finish();
                    }
                }
            }
        }, 1000);
    }


    public void internetAlert() {

        final Dialog dialog = new Dialog(Objects.requireNonNull(this));
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.nointernet);

        Button mYes, mNo;

        mYes=dialog.findViewById(R.id.btn_internetalert_yes);
        mNo=dialog.findViewById(R.id.btn_internetalert_no);

        mYes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SplashScreen.this, "Cool!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });

        mNo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(SplashScreen.this, "Bye then!", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                finish();
            }
        });

        dialog.show();

        }



    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onDestroy() {
        Log.v(LOG_TAG, "onDestory");
        super.onDestroy();
        unregisterReceiver(receiver);

    }

    public class NetworkChangeReceiver extends BroadcastReceiver {

        private boolean isNetworkAvailable(Context context) {
            ConnectivityManager connectivity = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (connectivity != null) {
                NetworkInfo[] info = connectivity.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            if(!isConnected){
                                Log.v(LOG_TAG, "Now you are connected to Internet!");
                                isConnected = true;
                                splash();
                            }
                            return true;
                        }
                    }
                }
            }
            Log.v(LOG_TAG, "You are not connected to Internet!");
            isConnected = false;
            internetAlert();
            return false;

        }

        @Override
        public void onReceive(Context context, Intent intent) {
            isNetworkAvailable(context);
        }
    }

}


