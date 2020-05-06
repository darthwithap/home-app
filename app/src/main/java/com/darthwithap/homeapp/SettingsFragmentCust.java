package com.darthwithap.homeapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

public class SettingsFragmentCust extends Fragment {

private Button profile, about;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        final View root = inflater.inflate(R.layout.fragment_settings_cust, container, false);

        profile=root.findViewById(R.id.btn_settings_profile_cust);
        about=root.findViewById(R.id.btn_settings_about_cust);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), CustomerSettings.class);
                startActivity(intent);
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Navigation.findNavController(v).navigate(R.id.nav_about_cust_frag);

            }
        });

        return root;
    }
}