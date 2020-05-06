package com.darthwithap.homeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignUp extends AppCompatActivity {

    private EditText mName, mEmail, mPassword, mPhone, mUsername;
    private RadioGroup radioGroup;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference ref, typeRef;
    private String mType;
    private Button signUp;
    private ImageView passwordIcon;
    private Dialog signUpDialog;
    private Boolean passHidden;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        mName=findViewById(R.id.et_name_signup);
        mEmail=findViewById(R.id.et_email_signup);
        mPassword=findViewById(R.id.et_password_signup);
        mPhone=findViewById(R.id.et_phone_signup);
        mUsername=findViewById(R.id.et_username_signup);
        signUp=findViewById(R.id.btn_su_signup);
        passwordIcon=findViewById(R.id.password_toggle_icon);
        radioGroup=findViewById(R.id.rgrp);
        mAuth=FirebaseAuth.getInstance();
        passHidden=true;
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isCorrectDetails()) signUpUser();
            }
        });

        passwordIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (passHidden){
                    passwordIcon.setImageResource(R.drawable.password_show);
                    mPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    passHidden=false;
                    mPassword.setSelection(mPassword.getText().length());
                }
                else {
                    passwordIcon.setImageResource(R.drawable.password_hide);
                    mPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    passHidden=true;
                    mPassword.setSelection(mPassword.getText().length());
                }
            }
        });
    }

    public boolean isCorrectDetails(){
        boolean flag=true;
        int selectType = radioGroup.getCheckedRadioButtonId();
        RadioButton radioButton = findViewById(selectType);
        if (radioButton!=null) mType=radioButton.getText().toString();
        if (mName.getText().toString().equals("")
                ||mUsername.getText().toString().equals("")
                ||mPhone.getText().toString().equals("")
                ||mEmail.getText().toString().equals("")
                ||mPassword.getText().toString().equals("")) {
            Toast.makeText(SignUp.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            flag=false;
        }
        else if (!isValidEmail(mEmail.getText().toString())){
            flag=false;
            Toast.makeText(SignUp.this, "Enter a valid email address", Toast.LENGTH_SHORT).show();
        }
        else if (!isValidPhoneNumber(mPhone.getText().toString())){
            flag=false;
            Toast.makeText(SignUp.this, "Enter a valid phone number", Toast.LENGTH_SHORT).show();
        }
        else if (mPassword.getText().toString().length()<6) {
            Toast.makeText(SignUp.this, "Minimum 6 characters required for password", Toast.LENGTH_SHORT).show();
            flag=false;
        }
        else if (radioButton==null) {
            flag=false;
            Toast.makeText(this, "You need chose a user type", Toast.LENGTH_SHORT).show();
        }

        return flag;
    }

    public void signUpUser() {
        signUpAlert();
        mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()){
                            mAuth.getCurrentUser().sendEmailVerification()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()){
                                                signUpDialog.dismiss();
                                                Toast.makeText(SignUp.this, "Registered successfully. Please verify your email", Toast.LENGTH_LONG).show();
                                                addUserInDatabase(mType+"s",mAuth.getCurrentUser().getUid());
                                                setUserInfo();
                                                if (mType.equals("Technician")) {
                                                    Intent i = new Intent(SignUp.this, TechnicianLoginActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                                else if (mType.equals("Customer")) {
                                                    Intent i = new Intent(SignUp.this, CustomerLoginActivity.class);
                                                    startActivity(i);
                                                    finish();
                                                }
                                            }
                                            else {
                                                Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    });
                        }
                        else {
                            Toast.makeText(SignUp.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void signUpAlert() {
        signUpDialog = new Dialog(Objects.requireNonNull(SignUp.this));
        Objects.requireNonNull(signUpDialog.getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        signUpDialog.setContentView(R.layout.signing_up);

        signUpDialog.show();
    }

    public void addUserInDatabase(String s,String User_Id) {
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        ref = mFirebaseDatabase.getReference("Users").child(s).child(User_Id);
        ref.setValue(true);
    }

    public boolean isValidEmail(String email)
    {
        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    public boolean isValidPhoneNumber(String phone) {
        Pattern p = Pattern.compile("^(\\+91[\\-\\s]?)?[0]?(91)?[789]\\d{9}$");
        Matcher m = p.matcher(phone);
        return m.matches();
    }

    public void setUserInfo() {
        typeRef=FirebaseDatabase.getInstance().getReference("UserType").child(mAuth.getCurrentUser().getUid());
        Map map = new HashMap();
        map.put("Name", mName.getText().toString());
        map.put("Phone", mPhone.getText().toString());
        map.put("Email", mEmail.getText().toString());
        map.put("Username", mUsername.getText().toString());
        ref.updateChildren(map);
        typeRef.setValue(mType);
    }

}
