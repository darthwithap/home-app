package com.darthwithap.homeapp;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.navigation.Navigation;

public class SubCategoryFragment extends Fragment implements AdapterView.OnItemSelectedListener{

    private Spinner spinner;
    private ArrayAdapter a0;
    private ArrayAdapter<String> a1;
    private ArrayAdapter<String> a2;
    private ArrayAdapter<String> a3;
    private ArrayAdapter<String> a4;
    private ArrayAdapter<String> a5;
    private ArrayAdapter<String> arrayAdapter;
    private Button bookNow, bookLater;
    private String jobSelected, categoryRequested;
    private String[] homeServices, repair, salon, household, help, event;
    private SharedViewModelTech viewModelTech;


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View root = inflater.inflate(R.layout.fragment_sub_category_tech, container, false);

        viewModelTech = ViewModelProviders.of(getActivity()).get(SharedViewModelTech.class);
        viewModelTech.getCategory().observe(getViewLifecycleOwner(), new Observer<String>() {
            @Override
            public void onChanged(String s) {
                categoryRequested = s;
                Log.e("Category", categoryRequested);

            }
        });

        spinner =root.findViewById(R.id.spinner);
        bookLater=root.findViewById(R.id.btn_book_later);
        bookNow=root.findViewById(R.id.btn_book_now);
        spinner.setOnItemSelectedListener(this);


        homeServices = new String[]{"Any Technician Nearby", "Electrician", "Plumber", "Carpenter", "Painter"};
        repair = new String[]{"Any Technician Nearby","Washing Machine", "AC Repair", "Fridge"};
        salon = new String[]{"Any Technician Nearby","Haircut", "Massage", "MakeUp", "Waxing", "Manicure", "Pedicure"};
        household = new String[]{"Any Technician Nearby","Pharmacy", "Water", "Dairy", "Grocery", "Laundry"};
        help = new String[]{"Any Technician Nearby","Cook", "Maid", "Washer", "Babysitter"};
        event = new String[]{"Any Technician Nearby","Birthday", "Anniversary", "Wedding"};

        a0=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,homeServices);
        a1=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,repair);
        a2=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,salon);
        a3=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,household);
        a4=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,help);
        a5=new ArrayAdapter<>(getActivity(),R.layout.spinner_text,event);

        //get category from view model;

        if (categoryRequested!=null){
            switch (categoryRequested){
                case "a0":
                    arrayAdapter=a0;
                    break;
                case "a1":
                    arrayAdapter=a1;
                    break;
                case "a2":
                    arrayAdapter=a2;
                    break;
                case "a3":
                    arrayAdapter=a3;
                    break;
                case "a4":
                    arrayAdapter=a4;
                    break;
                case "a5":
                    arrayAdapter=a5;
                    break;
            }
        }
        else {
            arrayAdapter=a0;
        }

        arrayAdapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(arrayAdapter);

        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobSelected!=null) {
                    viewModelTech.setJob(jobSelected);
                }
                Navigation.findNavController(v).navigate(R.id.nav_home_tech_frag);

            }
        });

        bookLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewModelTech.setJob("Any Technician Nearby");
                Navigation.findNavController(v).navigate(R.id.book_later_frag);
            }
        });

        return root;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        jobSelected = parent.getItemAtPosition(position).toString();
        updateRecyclerView(jobSelected);
    }

    private void updateRecyclerView(String jobSelected) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

}
