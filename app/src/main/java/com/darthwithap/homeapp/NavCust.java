package com.darthwithap.homeapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.firebase.geofire.GeoFire;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import androidx.drawerlayout.widget.DrawerLayout;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.widget.Toast;

public class NavCust extends AppCompatActivity {


    private AppBarConfiguration mAppBarConfigurationCust;
    public NavController navControllerCust;
    public ActionBarDrawerToggle mDrawerToggleCust;
    public DrawerLayout drawerCust;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_nav_cust);
        Toolbar toolbar = findViewById(R.id.toolbar_cust);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.account);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setDisplayShowHomeEnabled(false);
        drawerCust = findViewById(R.id.drawer_layout_cust);
        NavigationView navigationView = findViewById(R.id.nav_view_cust);
        mDrawerToggleCust = new ActionBarDrawerToggle(NavCust.this, drawerCust, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerCust.addDrawerListener(mDrawerToggleCust);
        mDrawerToggleCust.syncState();

        mAppBarConfigurationCust = new AppBarConfiguration.Builder(
                R.id.nav_home_cust, R.id.nav_bookings_cust, R.id.nav_reviews_cust, R.id.nav_settings_cust, R.id.nav_about_cust)
                .setDrawerLayout(drawerCust)
                .build();

        navControllerCust = Navigation.findNavController(NavCust.this, R.id.nav_host_fragment_cust);
        NavigationUI.setupActionBarWithNavController(NavCust.this, navControllerCust, mAppBarConfigurationCust);
        NavigationUI.setupWithNavController(navigationView, navControllerCust);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                openFragment(menuItem.getItemId());
                drawerCust.closeDrawer(GravityCompat.START,true);
                return true;
            }
        });

    }

    private void openFragment(int itemId) {
        switch (itemId) {
            case R.id.nav_home_cust:
                navControllerCust.navigate(R.id.nav_home_cust_frag);
                break;
            case R.id.nav_about_cust:
                navControllerCust.navigate(R.id.nav_about_cust_frag);
                break;
            case R.id.nav_bookings_cust:
                navControllerCust.navigate(R.id.nav_bookings_cust_frag);
                break;
            case R.id.nav_reviews_cust:
                navControllerCust.navigate(R.id.nav_reviews_cust_frag);
                break;
            case R.id.nav_settings_cust:
                navControllerCust.navigate(R.id.nav_settings_cust_frag);
                break;

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_cust, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_cust);
        return NavigationUI.navigateUp(navController, mAppBarConfigurationCust)
                || super.onSupportNavigateUp();
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
                Toast.makeText(NavCust.this, "Logout risk averted", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout_cust:
                logoutAlert();
                break;
            case R.id.action_about_cust:
                //Navigation.findNavController(getCurrentFocus()).navigate(R.id.nav_about_cust_frag);
                break;
            case R.id.action_help_cust:
                Intent i = new Intent(NavCust.this, Help.class);
                startActivity(i);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    public void logout_user() {
        removeLocationFromGeoFire();
        FirebaseAuth.getInstance().signOut();
        //isLoggedIn = false;
        SavedSharedPreference.clearUser(getApplicationContext());
        Intent i = new Intent(NavCust.this, MainActivity.class);
        startActivity(i);
        finish();
    }

    public void removeLocationFromGeoFire () {
        String user_id;
        user_id = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("TechniciansAvailable");

        GeoFire geoFire = new GeoFire(ref);
        geoFire.removeLocation(user_id);
    }
}
