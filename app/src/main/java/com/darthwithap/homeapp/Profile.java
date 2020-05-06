package com.darthwithap.homeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class Profile extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        /*Bundle bundle = getIntent().getExtras();
        if (bundle!=null) {
            if (bundle.getString(key)!=null){
                //Do whatever
            }
        }*/
    }
}
