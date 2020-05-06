package com.darthwithap.homeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class Payments extends AppCompatActivity {

    private Button payNow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payments);

        payNow=findViewById(R.id.btn_pay_now);

        payNow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.one97.paytm");
                if (launchIntent != null) {
                    startActivity(launchIntent);
                } else {
                    Toast.makeText(Payments.this, "There is no package available in android", Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
