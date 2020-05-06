package com.darthwithap.homeapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.firebase.geofire.GeoQuery;
import com.firebase.geofire.GeoQueryEventListener;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class HomeFragmentTech extends Fragment implements OnMapReadyCallback{

    private GoogleMap mMap;
    private Marker mMarker, techMarker;
    public Location mLocation;
    private LatLng custLocAtRequest;
    private final double INIT_RADIUS = 0.3;
    private double radius = INIT_RADIUS;
    private Boolean technicianFound = false;
    private Boolean firstTimeFlag;
    private Boolean isRequesting = false;
    private String technicianFoundID;

    private GeoQuery geoQuery;

    private DatabaseReference techLocRef, technicianFoundDatbaseRef;
    private ValueEventListener techLocRefListener;

    private ImageView customerProfile;
    private Dialog pairingDialog, bookingConfirmed;
    private int mapZoomLevel=15;

    private NavTech navTech;
    private Button cancel, request;

    private ConstraintLayout technicianInfo;
    private ImageView technicianProfile;
    private HashMap hashMap;

    private TextView technicianName, technicianPhone, technicianDesc, technicianRating;
    private String serviceCustID = "";
    private String job;

    private FirebaseRecyclerOptions<TechnicianCard> options;
    FirebaseRecyclerAdapter<TechnicianCard, TechCardViewHolder> adapter;
    private DatabaseReference custAssignedLocRef, customerDatabaseRef, technicianCardsRef;
    private ValueEventListener custAssignedLocRefListener;

    private RecyclerView mTechnicianCards;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest mLocationRequest;

    private LocationCallback mLocationCallback;
    private boolean isLoggedIn;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        navTech = (NavTech) getActivity();
        firstTimeFlag=true;
        isLoggedIn=true;

        View root = inflater.inflate(R.layout.fragment_home_tech, container, false);

        SupportMapFragment mapFragment = (SupportMapFragment)
                this.getChildFragmentManager()
                        .findFragmentById(R.id.map_tech);
        mapFragment.getMapAsync(this);

        setTechCardsDatabaseRef();
        technicianCardsRef.keepSynced(true);

        mTechnicianCards=root.findViewById(R.id.recycler_view);
        mTechnicianCards.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, true));

        firstTimeFlag = true;
        isLoggedIn = true;

        technicianInfo=root.findViewById(R.id.technician_info);
        technicianPhone=root.findViewById(R.id.txt_technician_phone);
        technicianDesc=root.findViewById(R.id.txt_technician_desc);
        technicianName=root.findViewById(R.id.txt_technician_name);
        technicianRating=root.findViewById(R.id.txt_technician_rating);
        customerProfile=root.findViewById(R.id.img_customer_profile);
        cancel=root.findViewById(R.id.btn_cancel);
        request=root.findViewById(R.id.btn_request);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        fetchLastLocation();
        mLocationCallback = new LocationCallback() {
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult.getLastLocation() == null) {
                    return;
                }

                for (Location location : locationResult.getLocations()) {
                    if (isLoggedIn) {
                        drawMaker(location);
                        mLocation=location;
                    }
                }
            }
        };

        mLocationRequest = createLocationRequest();

        options=new FirebaseRecyclerOptions.Builder<TechnicianCard>()
                .setQuery(technicianCardsRef, TechnicianCard.class).build();

        adapter=new FirebaseRecyclerAdapter<TechnicianCard, TechCardViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final TechCardViewHolder holder, int postion, @NonNull final TechnicianCard model) {
                holder.setJob(model.getJob());
                holder.setName(model.getName());
                if (model.getRating()!=null) holder.setRating(model.getRating());
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                       openTechDialog(model.getName(), model.getJob(), model.getEmail(), Double.toString(model.getRating()));
                    }
                });
            }

            @NonNull
            @Override
            public TechCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_layout_tech, parent, false);

            return new TechCardViewHolder(view);
            }
        };

        adapter.startListening();
        mTechnicianCards.setAdapter(adapter);

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                isRequesting=false;
                geoQuery.removeAllListeners();
                if (techLocRef!=null)
                    techLocRef.removeEventListener(techLocRefListener);

                if (technicianFoundID!=null) {
                    removeDetailsFromFireBase();
                    technicianFoundID=null;
                }
                technicianFound = false;
                radius = INIT_RADIUS;

                removeLocationFromGeoFire();
                cancel.setClickable(false);
                technicianInfo.setVisibility(View.GONE);
                technicianName.setText("");
                technicianPhone.setText("");
                technicianDesc.setText("");
                //technicianProfile.setImageResource(R.mipmap.img_profile_icon);
                cancel.setVisibility(View.INVISIBLE);
                request.setText("Request for a technician");
                request.setClickable(true);

            }
        });

        request.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isRequesting) {
                    showPairingBox();
                    isRequesting=true;
                    request.setText("Requesting for a technician...");
                    cancel.setVisibility(View.VISIBLE);
                    cancel.setClickable(true);
                    request.setClickable(false);
                    Log.e("mLocation: ", mLocation.toString());
                    custLocAtRequest = new LatLng(mLocation.getLatitude(), mLocation.getLongitude());
                    sendLocationToGeoFire(mLocation);
                    getClosestTechnician();
                }
            }
        });

        return root;
    }

    private void showPairingBox() {
         pairingDialog = new Dialog(Objects.requireNonNull(getActivity()));
        Objects.requireNonNull(pairingDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        pairingDialog.setContentView(R.layout.pairing_loader);

        pairingDialog.show();
    }

    private void removeDetailsFromFireBase() {
        DatabaseReference techRef = FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(technicianFoundID).child("serviceCustomerID");
        techRef.removeValue();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomersLinked")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        ref.removeValue();
    }

    private void getClosestTechnician() {
        DatabaseReference techLoc = FirebaseDatabase.getInstance().getReference().child("TechniciansAvailable");
        GeoFire geoFire = new GeoFire(techLoc);

        geoQuery = geoFire.queryAtLocation(new GeoLocation(custLocAtRequest.latitude, custLocAtRequest.longitude), radius);
        geoQuery.removeAllListeners();
        geoQuery.addGeoQueryEventListener(new GeoQueryEventListener() {
            @Override
            public void onKeyEntered(String key, GeoLocation location) {
                if (!technicianFound && isRequesting) {
                    pairingDialog.dismiss();
                    bookingConfirmedAlert();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            bookingConfirmed.dismiss();
                        }
                    },1500);
                    technicianFound=true;
                    technicianFoundID = key;
                    DatabaseReference techRef = FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(technicianFoundID);
                    String serviceCustomerID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                    hashMap = new HashMap();
                    hashMap.put("serviceCustomerID", serviceCustomerID );
                    techRef.updateChildren(hashMap);
                    removeCustomerRequests(mLocation);
                    getTechnicianLocation();
                    request.setText("Fetching Technician Location...");
                    getTechnicianInfo();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent i = new Intent(getActivity(), Payments.class);
                            startActivity(i);
                        }
                    },15000);
                }
            }


            public void onKeyExited(String key) {

            }

            public void onKeyMoved(String key, GeoLocation location) {

            }

            public void onGeoQueryReady() {
                if (!technicianFound){
                    radius+=0.3;
                    getClosestTechnician();
                }
            }

            public void onGeoQueryError(DatabaseError error) {

            }
        });
    }

    private void bookingConfirmedAlert() {
        bookingConfirmed = new Dialog(Objects.requireNonNull(getActivity()));
        Objects.requireNonNull(bookingConfirmed.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        bookingConfirmed.setContentView(R.layout.booking_confirmed);

        bookingConfirmed.show();
    }

    private void removeCustomerRequests(Location location) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequests");
        DatabaseReference refNew = FirebaseDatabase.getInstance().getReference("CustomersLinked");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);

        GeoFire geoFireNew = new GeoFire(refNew);
        geoFireNew.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));

    }


    private void getTechnicianLocation(){
        techLocRef = FirebaseDatabase.getInstance().getReference("TechniciansWorking").child(technicianFoundID ).child("l");
        technicianFoundDatbaseRef= FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(technicianFoundID);
        techLocRefListener =  techLocRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && isRequesting) {
                    List<Object> map = (List<Object>) dataSnapshot.getValue();
                    double locLat = 0;
                    double locLng = 0;
                    request.setText("TechnicianFound");
                    if (map.get(0)!=null) {
                        locLat = Double.parseDouble(map.get(0).toString());
                    }
                    if (map.get(1)!=null) {
                        locLng = Double.parseDouble(map.get(1).toString());
                    }

                    LatLng techLatLng = new LatLng(locLat, locLng);
                    if (techMarker!=null) {
                        techMarker.remove();
                    }

                    Location loc1 = new Location("");
                    loc1.setLatitude(custLocAtRequest.latitude);
                    loc1.setLongitude(custLocAtRequest.longitude);

                    Location loc2 = new Location("");
                    loc2.setLatitude(techLatLng.latitude);
                    loc2 .setLongitude(techLatLng.longitude);

                    float distance = loc1.distanceTo(loc2);

                    if (distance<100) {
                        //info.setText("The technician is almost here...");
                    }
                    else if (distance>=100) {
                        //info.setText("Driver distance: "+ String.valueOf(distance));

                    }

                    techMarker = mMap.addMarker(new MarkerOptions().position(techLatLng).title("Your technician here"));

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void getTechnicianInfo() {
        technicianInfo.setVisibility(View.VISIBLE);
        technicianFoundDatbaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("Name")!=null) {
                        technicianName.setText(map.get("Name").toString());
                    }
                    if (map.get("Phone")!=null) {
                        technicianPhone.setText(map.get("Phone").toString());
                    }
                    if (map.get("Username")!=null) {
                        technicianDesc.setText(map.get("Username").toString());
                    }
                    if (map.get("Rating")!=null) {
                        technicianDesc.setText(map.get("Rating").toString());
                    }
                    if (map.get("Profile Image Url")!=null) {
                        Glide.with(getActivity()).load(map.get("Profile Image Url").toString()).into(technicianProfile);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void openTechDialog(String name, String job, String desc, String rating) {
        final Dialog dialog = new Dialog(Objects.requireNonNull(getActivity()));
        Objects.requireNonNull(dialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.custom_tech_details_dialog);

        TextView mName, mDesc, mJob, mRating;
        Button mBookNow;

        mName=dialog.findViewById(R.id.txt_name_techdetails_dialog);
        mDesc=dialog.findViewById(R.id.txt_desc_techdetails_dialog);
        mJob=dialog.findViewById(R.id.txt_job_techdetails_dialog);
        mRating=dialog.findViewById(R.id.txt_rating_techdetails_dialog);
        mBookNow=dialog.findViewById(R.id.btn_book_now_techdetails_dialog);

        mName.setText(name);
        mDesc.setText(desc);
        mRating.setText(rating);
        mJob.setText(job);
        mBookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(navTech, "Kindly book then!", Toast.LENGTH_SHORT).show();
                   dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void setTechCardsDatabaseRef() {
        technicianCardsRef=FirebaseDatabase.getInstance().getReference("Users").child("Technicians");
    }


    private void fetchLastLocation() {

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
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

    private void drawMaker(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        if (mMarker != null)
            mMarker.setPosition(latLng);
        if (mMap != null) {
            mMap.clear();
            MarkerOptions mMarkerOptions = new MarkerOptions();
            mMarkerOptions.position(latLng).title("Current Location");
            mMarker = mMap.addMarker(mMarkerOptions);
            if (firstTimeFlag)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, mapZoomLevel));

        }
    }

    private LocationRequest createLocationRequest() {
        LocationRequest mLocationRequest = LocationRequest.create();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(500);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }



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

    public void sendLocationToGeoFire(Location location) {
        String user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequests");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.setLocation(user_id, new GeoLocation(location.getLatitude(), location.getLongitude()));
    }


    public void removeLocationFromGeoFire() {
        String user_id;
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequests");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (adapter!=null) adapter.startListening();
    }

    @Override
    public void onStop() {
        if (adapter!=null) adapter.stopListening();
        super.onStop();

    }

    @Override
    public void onResume() {
        super.onResume();
        if (adapter!=null) adapter.startListening();
    }

    private class TechCardViewHolder extends RecyclerView.ViewHolder {

        TextView name, job, rating;

        public TechCardViewHolder(@NonNull View itemView) {
            super(itemView);

            name=itemView.findViewById(R.id.txt_name_card);
            job=itemView.findViewById(R.id.txt_job_card);
            rating=itemView.findViewById(R.id.txt_rating_card);

        }

        public void setName(String Name) {
            name.setText(Name);
        }

        public void setJob(String Job) {
            job.setText(Job);
        }

        public void setRating(Double Rating) {
            rating.setText(Double.toString(Rating));
        }



    }
}
