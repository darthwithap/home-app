package com.darthwithap.homeapp;

import android.content.DialogInterface;
import android.content.Intent;

import android.os.Bundle;
import android.view.MenuItem;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.firebase.geofire.GeoFire;
import com.google.android.material.navigation.NavigationView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.View;
import android.widget.Toast;

public class NavTech extends AppCompatActivity{


    private AppBarConfiguration mAppBarConfigurationTech;
    public NavController navControllerTech;
    public ActionBarDrawerToggle mDrawerToggleTech;
    public DrawerLayout drawerTech;
    private String jobDemanded;
    private SharedViewModelTech viewModelTech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModelTech = ViewModelProviders.of(this).get(SharedViewModelTech.class);
        viewModelTech.getJob().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                jobDemanded = s;
            }
        });


        setContentView(R.layout.activity_nav_tech);
        final Toolbar toolbar = findViewById(R.id.toolbar_tech);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        drawerTech = findViewById(R.id.drawer_layout_tech);
        NavigationView navigationView = findViewById(R.id.nav_view_tech);
        mDrawerToggleTech = new ActionBarDrawerToggle(this, drawerTech, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerTech.addDrawerListener(mDrawerToggleTech);
        mDrawerToggleTech.syncState();

        mAppBarConfigurationTech = new AppBarConfiguration.Builder(
                R.id.nav_home_tech, R.id.nav_bookings_tech,
                R.id.nav_services_tech, R.id.nav_reviews_tech, R.id.nav_settings_tech)
                .setDrawerLayout(drawerTech)
                .build();

        navControllerTech = Navigation.findNavController(this, R.id.nav_host_fragment_tech);
        NavigationUI.setupActionBarWithNavController(this, navControllerTech, mAppBarConfigurationTech);
        NavigationUI.setupWithNavController(navigationView, navControllerTech);

        navControllerTech.addOnDestinationChangedListener(new NavController.OnDestinationChangedListener() {
            @Override
            public void onDestinationChanged(@NonNull NavController controller, @NonNull NavDestination destination, @Nullable Bundle arguments) {
                if (destination.getId()==R.id.sub_category_frag || destination.getId()==R.id.book_later_frag){
                    toolbar.setVisibility(View.GONE);
                }
                else
                {
                    toolbar.setVisibility(View.VISIBLE);
                }
            }
        });

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                openFragment(menuItem.getItemId());
                drawerTech.closeDrawer(GravityCompat.START,true);
                return true;
            }
        });

    }

    private void openFragment(int itemId) {
        switch (itemId) {
            case R.id.nav_home_tech:
                navControllerTech.navigate(R.id.nav_home_tech_frag);
                break;
            case R.id.nav_services_tech:
                navControllerTech.navigate(R.id.nav_services_tech_frag);
                break;
            case R.id.nav_bookings_tech:
                navControllerTech.navigate(R.id.nav_bookings_tech_frag);
                break;
            case R.id.nav_reviews_tech:
                navControllerTech.navigate(R.id.nav_reviews_tech_frag);
                break;
            case R.id.nav_settings_tech:
                navControllerTech.navigate(R.id.nav_settings_tech_frag);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_tech, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_tech);
        return NavigationUI.navigateUp(navController, mAppBarConfigurationTech)
                || super.onSupportNavigateUp();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout_tech:
                logoutAlert();
                break;
            case R.id.action_about_tech:
                openAboutActivity();
                break;
            case R.id.action_help_tech:
                Intent i = new Intent(NavTech.this, Help.class);
                startActivity(i);
                openHelpActivity();
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private void openHelpActivity() {

    }

    private void openAboutActivity() {

    }

    public void logoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Do you really want to logout?").setTitle("Logout alert");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               logout_user();
            }
        });

        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(NavTech.this, "Logout risk averted", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void logout_user() {
        removeLocationFromGeoFire();
        FirebaseAuth.getInstance().signOut();
        //isLoggedIn = false;
        SavedSharedPreference.clearUser(getApplicationContext());
        Intent i = new Intent(NavTech.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void removeLocationFromGeoFire() {
        String user_id;
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("CustomerRequests");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);
    }

}

