package com.darthwithap.homeapp;


import android.content.res.Resources;
import android.location.Location;
import android.os.Bundle;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.directions.route.AbstractRouting;
import com.directions.route.Route;
import com.directions.route.RouteException;
import com.directions.route.Routing;
import com.directions.route.RoutingListener;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

public class HomeFragmentCust extends Fragment implements OnMapReadyCallback, RoutingListener {

    private GoogleMap mMap;
    private Marker mMarker, customerMarker ;

    private ConstraintLayout customerInfo;
    private ImageView customerProfile;

    private TextView customerName, customerPhone, customerEmail;

    private Button logout, settings;
    private Location mLocation;

    private String serviceCustID = "";
    private boolean firstTimeFlag, isLoggedIn;

    private DatabaseReference custAssignedLocRef, customerDatabaseRef;
    private ValueEventListener custAssignedLocRefListener;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback mLocationCallback;
    private LocationRequest mLocationRequest;
    private List<Polyline> polylines;
    private static final int[] COLORS = new int[]{R.color.appColor};



    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_home_cust, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                this.getChildFragmentManager()
                        .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        firstTimeFlag = true;
        isLoggedIn = true;
        polylines = new ArrayList<>();

        customerInfo=root.findViewById(R.id.customer_info);
        customerPhone=root.findViewById(R.id.txt_customer_phone);
        customerEmail=root.findViewById(R.id.txt_customer_email);
        customerName=root.findViewById(R.id.txt_customer_name);
        customerProfile=root.findViewById(R.id.img_customer_profile);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLastLocation();
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (isLoggedIn) {
                        mLocation=location;
                        drawMaker(location);
                    }
                }
            }
        };

        mLocationRequest = createLocationRequest();
        customerDatabaseRef= FirebaseDatabase.getInstance().getReference("Users").child("Customers").child(serviceCustID);

        if (isLoggedIn) {
            getCustomerAssigned();
        }

        return root;
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }


    private void fetchLastLocation() {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null)
                        {
                            if (firstTimeFlag && mMap != null) {
                                drawMaker(location);
                                mLocation=location;
                                firstTimeFlag = false;
                            }

                            Log.e("Last Location: ", location.toString());
                        }
                    }
                });
    }

    private void getCustomerAssigned() {
        String technicianID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference custAssignedRef = FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(technicianID).child("serviceCustomerID");
        custAssignedRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    serviceCustID = dataSnapshot.getValue().toString();
                    sendLocationToGeoFire(mLocation);
                    getCustomerAssignedLocation();
                    getCustomerAssignedInfo();
                }
                else {
                    if (customerMarker!=null) {
                        customerMarker.remove();
                    }
                    if (custAssignedLocRef!=null)
                        custAssignedLocRef.removeEventListener(custAssignedLocRefListener);
                    customerInfo.setVisibility(View.GONE);
                    serviceCustID=null;
                    erasePolyLines();
                    sendLocationToGeoFire(mLocation);
                    customerName.setText("");
                    customerPhone.setText("");
                    customerEmail.setText("");
                    //customerProfile.setImageResource(R.mipmap.img_profile_icon);

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
                        Glide.with(getActivity()).load(map.get("Profile Image Url").toString()).into(customerProfile   );
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getCustomerAssignedLocation() {
        custAssignedLocRef = FirebaseDatabase.getInstance().getReference("CustomerLinked").child(serviceCustID).child("l");
        custAssignedLocRefListener = custAssignedLocRef.addValueEventListener(new ValueEventListener() {
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
                    getRouteToMarker(custLocLat);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void getRouteToMarker(LatLng locLat) {
        Routing routing = new Routing.Builder()
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(this)
                .alternativeRoutes(true)
                .waypoints(new LatLng(mLocation.getLatitude(), mLocation.getLongitude()) , locLat)
                .build();
        routing.execute();
    }


    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        try {
            boolean success = mMap.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                            getContext(), R.raw.style_json));

            if (!success) {
                Log.e("tag", "Style parsing failed.");
            }
        } catch (Resources.NotFoundException e) {
            Log.e("tag", "Can't find style. Error: ", e);
        }

        mMap.setMyLocationEnabled(true);

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

            sendLocationToGeoFire(location);
        }

    }

    @Override
    public void onPause() {
        super.onPause();

    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void sendLocationToGeoFire(Location location) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference refAvailable = FirebaseDatabase.getInstance().getReference("TechniciansAvailable");
        DatabaseReference refWorking = FirebaseDatabase.getInstance().getReference("TechniciansWorking");

        GeoFire geoFireAvailable = new GeoFire(refAvailable);
        GeoFire geoFireWorking = new GeoFire(refWorking );

        if (serviceCustID==null){
            geoFireWorking.removeLocation(user_id);
            geoFireAvailable.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
        }
        else {
            geoFireAvailable.removeLocation(user_id);
            geoFireWorking .setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
        }
    }


    @Override
    public void onRoutingFailure(RouteException e) {
        if(e != null) {
            Toast.makeText(getActivity(), "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }else {
            Toast.makeText(getActivity(), "Something went wrong, Try again", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingStart() {

    }

    @Override
    public void onRoutingSuccess(ArrayList<Route> route, int shortestRouteIndex) {
        if(polylines.size()>0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        polylines = new ArrayList<>();
        //add route(s) to the map.
        for (int i = 0; i <route.size(); i++) {

            int colorIndex = i % COLORS.length;

            PolylineOptions polyOptions = new PolylineOptions();
            polyOptions.color(getResources().getColor(COLORS[colorIndex]));
            polyOptions.width(10 + i * 3);
            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = mMap.addPolyline(polyOptions);
            polylines.add(polyline);

            Toast.makeText(getActivity() ,"Route "+ (i+1) +": distance - "+ route.get(i).getDistanceValue()+": duration - "+ route.get(i).getDurationValue(),Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRoutingCancelled() {

    }

    public void erasePolyLines() {
        for (Polyline polyline:polylines) {
            polyline.remove();
        }
    }



    public void addToBookings() {
        DatabaseReference techRef = FirebaseDatabase.getInstance().getReference("Users").child("Technicians")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("Bookings");
        DatabaseReference custRef = FirebaseDatabase.getInstance().getReference("Users").child("Customers")
                .child(serviceCustID).child("Bookings");
        DatabaseReference bookingsRef = FirebaseDatabase.getInstance().getReference("Bookings");
        String serviceID = bookingsRef.push().getKey();
        techRef.child(serviceID).setValue(true);
        custRef.child(serviceID).setValue(true);

        HashMap map = new HashMap();
        map.put("Customer", serviceCustID);
        map.put("Rating", 5);
        bookingsRef.child(serviceID).updateChildren(map);

    }
}