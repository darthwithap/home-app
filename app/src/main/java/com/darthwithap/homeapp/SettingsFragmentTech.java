package com.darthwithap.homeapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;

public class SettingsFragmentTech extends Fragment {

    private Button profile, about, logout;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_settings_tech, container, false);

        profile=root.findViewById(R.id.btn_settings_profile_tech);
        about=root.findViewById(R.id.btn_settings_about_tech);
        logout=root.findViewById(R.id.btn_settings_logout_tech);

        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), TechnicianSettings.class);
                startActivity(intent);
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                logoutAlert();
            }
        });

        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Intent;
            }
        });

        return root;
    }

    public void logoutAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
                Toast.makeText(getActivity(), "Logout risk averted", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();

    }

    public void logout_user() {
        FirebaseAuth.getInstance().signOut();
        //isLoggedIn = false;
        SavedSharedPreference.clearUser(getActivity());
        Intent i = new Intent(getActivity(), MainActivity.class);
        startActivity(i);
        getActivity().finish();
    }
}