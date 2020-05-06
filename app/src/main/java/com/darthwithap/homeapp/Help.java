package com.darthwithap.homeapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class Help extends AppCompatActivity {

    private TextView support;
    private ImageView call;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        support=findViewById(R.id.support_btn);
        call=findViewById(R.id.call_btn);

        final Uri u = Uri.parse("tel:9999999999");

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                startActivity(i);

            }
        });

        support.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_DIAL, u);
                startActivity(i);
            }
        });
    }
}
