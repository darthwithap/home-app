package com.darthwithap.homeapp;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;
import java.util.Map;

public class TechnicianMap extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private Marker mMarker, customerMarker ;

    private LinearLayout customerInfo;
    private ImageView customerProfile;

    private TextView customerName, customerPhone, customerEmail;

    private Button logout, settings;

    private String serviceCustID = "";

    private DatabaseReference custAssignedLocRef, customerDatabaseRef;
    private ValueEventListener custAssignedLocRefListener;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;
    private LocationSettingsRequest.Builder builder;
    private boolean firstTimeFlag, isLoggedIn;
    private static final int REQUEST_CHECK_SETTINGS = 102;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_technician_map);
        logout = findViewById(R.id.btn_logout);
        settings = findViewById(R.id.btn_settings);
        firstTimeFlag = true;
        isLoggedIn = true;

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        customerInfo=findViewById(R.id.customer_info);
        customerPhone=findViewById(R.id.txt_customer_phone);
        customerEmail=findViewById(R.id.txt_customer_email);
        customerName=findViewById(R.id.txt_customer_name);
        customerProfile=findViewById(R.id.img_customer_profile);

        customerDatabaseRef= FirebaseDatabase.getInstance().getReference("Users").child("Customers").child(serviceCustID);

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeLocationFromGeoFire();
                FirebaseAuth.getInstance().signOut();
                isLoggedIn = false;
                SavedSharedPreference.clearUser(getApplicationContext());
                Intent i = new Intent(TechnicianMap.this, MainActivity.class);
                startActivity(i);
                finish();
                return;
            }
        });

        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(TechnicianMap.this, TechnicianSettings.class);
                startActivity(intent);
                return;
            }
        });

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fetchLastLocation();
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (isLoggedIn) {
                        drawMaker(location);
                        Log.e("Continuous Location: ", location.toString());
                    }

                }
            }
        };

        mLocationRequest = createLocationRequest();
        builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        checkLocationSetting(builder);

        if (isLoggedIn) {
            getCustomerAssigned();
        }
    }

    private void getCustomerAssigned() {
        String technicianID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference custAssignedRef = FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(technicianID).child("serviceCustomerID");
        custAssignedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    serviceCustID = dataSnapshot.getValue().toString();
                    getCustomerAssignedLocation();
                    getCustomerAssignedInfo();
                }
                else {
                    serviceCustID = "";
                    if (customerMarker!=null) {
                        customerMarker.remove();
                    }
                    if (custAssignedLocRef!=null)
                        custAssignedLocRef.removeEventListener(custAssignedLocRefListener);
                    customerInfo.setVisibility(View.GONE);
                    customerName.setText("");
                    customerPhone.setText("");
                    customerEmail.setText("");
                    customerProfile.setImageResource(R.mipmap.img_profile_icon);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerAssignedInfo() {
        customerInfo.setVisibility(View.VISIBLE);
        customerDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("Name")!=null) {
                        customerName.setText(map.get("Name").toString());
                    }
                    if (map.get("Phone")!=null) {
                        customerPhone.setText(map.get("Phone").toString());
                    }
                    if (map.get("Email")!=null) {
                        customerEmail.setText(map.get("Email").toString());
                    }
                    if (map.get("Profile Image Url")!=null) {
                        Glide.with(TechnicianMap.this).load(map.get("Profile Image Url").toString()).into(customerProfile   );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerAssignedLocation() {
        custAssignedLocRef = FirebaseDatabase.getInstance().getReference("CustomerRequests").child(serviceCustID).child("l");
        custAssignedLocRefListener =   custAssignedLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && serviceCustID!=null)  {
                     List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locLat = 0;
                    double locLng = 0;
                    if (map.get(0)!=null) {
                        locLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null) {
                        locLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng custLocLat = new LatLng(locLat, locLng);
                    customerMarker = mMap.addMarker(new MarkerOptions().position(custLocLat ).title("Your customer here"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

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
                            if (firstTimeFlag && mMap != null) {
                                drawMaker(location);
                                firstTimeFlag = false;
                            }

                            Log.e("Last Location: ", location.toString());
                        }
                    }
                });
    }


    @Override
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

    private void showPermissionAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 123);
        }
    }

    private void checkLocationSetting(final LocationSettingsRequest.Builder builder) {
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
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(TechnicianMap.this);
                    builder1.setMessage("Continuous Location Request");
                    builder1.setTitle("Location Request");
                    builder1.create();
                    builder1.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                            try {
                                resolvableApiException.startResolutionForResult(TechnicianMap.this, REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    builder1.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            Toast.makeText(TechnicianMap.this, "Location Update Permission Denied", Toast.LENGTH_SHORT).show();
                        }
                    });
                    builder1.show();
                }
            }
        });
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

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

        }
        mMap.setMyLocationEnabled(true);
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isLoggedIn) removeLocationFromGeoFire();

    }

    public void drawMaker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMarker != null)
            mMarker.setPosition(latLng);
        if (mMap != null) {
            mMap.clear();
            MarkerOptions mMarkerOptions = new MarkerOptions();
            mMarkerOptions.position(latLng).title("Current Location");
            mMarker = mMap.addMarker(mMarkerOptions);
            if (firstTimeFlag)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18));

                //Sending location to firebase using GeoFire
                String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("TechniciansAvailable");
                DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("TechniciansWorking");

            GeoFire geoFireAvailable = new GeoFire(refAvailable);
            GeoFire geoFireWorking = new GeoFire(refWorking );

            switch (serviceCustID) {
                    case "":
                        geoFireWorking.removeLocation(user_id);
                        geoFireAvailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));

                        break;
                    default:
                        geoFireAvailable.removeLocation(user_id);
                        geoFireWorking .setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));

                        break;
                }


            }

        }



            @Override
            protected void onStop () {
                super.onStop();
                if (isLoggedIn) removeLocationFromGeoFire();
            }

            public void removeLocationFromGeoFire () {
                String user_id;
                user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TechniciansAvailable");

                GeoFire geoFire = new GeoFire(ref);
                geoFire.removeLocation(user_id);
            }

    }

