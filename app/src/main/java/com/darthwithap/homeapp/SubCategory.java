package com.darthwithap.homeapp;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class SubCategory extends FragmentActivity implements AdapterView.OnItemSelectedListener {

    private Spinner spinner;
    private ArrayAdapter<String> a0,a1,a2,a3,a4,a5, arrayAdapter;
    private String i;
    private Button bookNow, bookLater;
    private String jobSelected;
    private String[] homeServices, repair, salon, household, help, event;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_sub_category_tech);

        spinner = findViewById(R.id.spinner);
        bookLater=findViewById(R.id.btn_book_later);
        bookNow=findViewById(R.id.btn_book_now);
        spinner.setOnItemSelectedListener(SubCategory.this);

        homeServices = new String[]{"Any Technician Nearby", "Electrician", "Plumber", "Carpenter", "Painter"};
        repair = new String[]{"Any Technician Nearby","Washing Machine", "AC Repair", "Fridge"};
        salon = new String[]{"Any Technician Nearby","Haircut", "Massage", "MakeUp", "Waxing", "Manicure", "Pedicure"};
        household = new String[]{"Any Technician Nearby","Pharmacy", "Water", "Dairy", "Grocery", "Laundry"};
        help = new String[]{"Any Technician Nearby","Cook", "Maid", "Washer", "Babysitter"};
        event = new String[]{"Any Technician Nearby","Birthday", "Anniversary", "Wedding"};

        a0=new ArrayAdapter<>(this,R.layout.spinner_text,homeServices);
        a1=new ArrayAdapter<>(this,R.layout.spinner_text,repair);
        a2=new ArrayAdapter<>(this,R.layout.spinner_text,salon);
        a3=new ArrayAdapter<>(this,R.layout.spinner_text,household);
        a4=new ArrayAdapter<>(this,R.layout.spinner_text,help);
        a5=new ArrayAdapter<>(this,R.layout.spinner_text,event);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            if (bundle.getString("CategoryId") != null) {
                i = bundle.getString("CategoryId");
                Log.e("Service id",i);

            }
        }

        switch (i){
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
        
        arrayAdapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(arrayAdapter);

        bookNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (jobSelected==null || jobSelected.equals("Any Technician Nearby")) {
                    Intent intent = new Intent(SubCategory.this, NavTech.class);
                    intent.putExtra("jobRequested", jobSelected);
                    startActivity(intent);
                    finish();
                }
            }
        });

        bookLater.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        jobSelected = parent.getItemAtPosition(position).toString();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
