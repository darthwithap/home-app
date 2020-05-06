package com.darthwithap.homeapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class ServicesFragmentTech extends Fragment {

    private Button search;
    private ImageButton homeservices, repair, salon, household, help, event;
    private SharedViewModelTech viewModelTech;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_services_tech, container, false);

        viewModelTech = ViewModelProviders.of(getActivity()).get(SharedViewModelTech.class);
        search=root.findViewById(R.id.btn_services_search_tech);

        homeservices=root.findViewById(R.id.imageButton1);
        repair=root.findViewById(R.id.imageButton2);
        salon=root.findViewById(R.id.imageButton3);
        household=root.findViewById(R.id.imageButton4);
        help=root.findViewById(R.id.imageButton5);
        event=root.findViewById(R.id.imageButton6);


        homeservices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a0");
            }
        });

        repair.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a1");
            }
        });

        salon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a2");
            }
        });

        household.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a3");
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a4");
            }
        });

        event.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeFragment(v, "a5");
            }
        });

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModelTech.setJob("Any Technician Nearby");
                Navigation.findNavController(v).navigate(R.id.nav_home_tech_frag);
            }
        });

        return root;
    }
    
    public void changeFragment(View view, String category){
        viewModelTech.setCategory(category);
        Navigation.findNavController(view).navigate(R.id.sub_category_frag);
    }

}