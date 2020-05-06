package com.darthwithap.homeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.Objects;

public class CustomerLoginActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    DatabaseReference ref;
    private boolean flag;
    private Button login;
    private EditText email, password;
    private ImageView passwordIcon;
    private Boolean passHidden;
    private Dialog loginDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_login);
        passHidden=true;
        mAuth=FirebaseAuth.getInstance();
        passwordIcon=findViewById(R.id.password_toggle_icon);
        login=findViewById(R.id.btn_si_cust);
        email=findViewById(R.id.et_email_cust);
        password=findViewById(R.id.et_password_cust);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                LogInAlert();
                mAuth.signInWithEmailAndPassword(email.getText().toString(), password.getText().toString())
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()){

                                    ref=FirebaseDatabase.getInstance().getReference("UserType").child(mAuth.getCurrentUser().getUid());
                                    ref.addValueEventListener(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                            if (dataSnapshot.getValue().toString().equals("Customer")) {
                                                if (mAuth.getCurrentUser().isEmailVerified()){
                                                    SavedSharedPreference.setUserId(getApplicationContext(), mAuth.getCurrentUser().getUid());
                                                    SavedSharedPreference.setCustomerType(getApplicationContext(), "Customer");
                                                    loginDialog.dismiss();
                                                    startActivity(new Intent(CustomerLoginActivity.this, NavTech.class));
                                                    finish();
                                                }
                                                else{
                                                    loginDialog.dismiss();
                                                    Toast.makeText(CustomerLoginActivity.this, "Please verify your email address", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else {
                                                loginDialog.dismiss();
                                                Toast.makeText(CustomerLoginActivity.this, "Incorrect email or password", Toast.LENGTH_SHORT).show();
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError databaseError) {
                                        }
                                    });
                                }
                                else {
                                    loginDialog.dismiss();
                                    Toast.makeText(CustomerLoginActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });

        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passHidden){
                    passwordIcon.setImageResource(R.drawable.password_show);
                    password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passHidden=false;
                    password.setSelection(password.getText().length());
                }
                else {
                    passwordIcon.setImageResource(R.drawable.password_hide);
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passHidden=true;
                    password.setSelection(password.getText().length());
                }
            }
        });
        
    }

    private void LogInAlert() {
        loginDialog = new Dialog(Objects.requireNonNull(CustomerLoginActivity.this));
        Objects.requireNonNull(loginDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loginDialog.setContentView(R.layout.logging_in);

        loginDialog.show();
    }

    
}
