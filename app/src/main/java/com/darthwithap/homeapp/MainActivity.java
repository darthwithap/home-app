package com.darthwithap.homeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity {

    private Button tech, cust;
    private TextView signup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tech=findViewById(R.id.btn_tech);
        cust=findViewById(R.id.btn_cust);
        signup=findViewById(R.id.txt_signup);

        tech.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TechnicianLoginActivity.class);
                startActivity(i);
            }
        });

        cust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), CustomerLoginActivity.class);
                startActivity(i);
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), SignUp.class);
                startActivity(i);
            }
        });
    }
}
