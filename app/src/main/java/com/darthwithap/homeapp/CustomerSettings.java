package com.darthwithap.homeapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CustomerSettings extends AppCompatActivity {

    private EditText name, phone, email, desc, username;
    private Button back, confirm;
    private ArrayAdapter<String> arrayAdapter;
    private ImageView profileImg;

    private FirebaseAuth mAuth;
    private DatabaseReference customerDatabaseRef;
    private String[] categories;

    private String user_id, mName, mEmail, mPhone, mProfileImageUrl, mUsername, mDesc, initEmail;
    private Spinner spinner;
    private int finFlag=-1;
    private Uri resultUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_customer_settings);

        categories = new String[]{"Electrician", "Plumber", "Carpenter", "Painter",
                "Washing Machine", "Air Conditioner", "Fridge",
                "Haircut", "Massage", "MakeUp", "Waxing", "Manicure", "Pedicure",
                "Pharmacy", "Water", "Dairy", "Grocery", "Laundry",
                "Cook", "Maid", "Washer", "Babysitter",
                "Birthday", "Anniversary", "Wedding"};

        spinner=findViewById(R.id.spinner_settings_cust);

        arrayAdapter = new ArrayAdapter<>(this, R.layout.spinner_text, categories);
        arrayAdapter.setDropDownViewResource(R.layout.spinner_text);
        spinner.setAdapter(arrayAdapter);

        name=findViewById(R.id.et_name_settings_cust);
        phone=findViewById(R.id.et_phone_settings_cust);
        email=findViewById(R.id.et_email_settings_cust);
        username=findViewById(R.id.et_username_settings_cust);
        desc=findViewById(R.id.et_desc_settings_cust);

        back=findViewById(R.id.btn_back_settings_cust);
        confirm=findViewById(R.id.btn_confirm_settings_cust);

        profileImg=findViewById(R.id.img_profile_settings_cust);

        mAuth=FirebaseAuth.getInstance();

        user_id=mAuth.getCurrentUser().getUid();
        customerDatabaseRef= FirebaseDatabase.getInstance().getReference("Users").child("Technicians").child(user_id);

        initEmail=getUserInfo();

        profileImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 1);
            }
        });

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mName=name.getText().toString();
                mEmail=email.getText().toString();
                mPhone=phone.getText().toString();
                mUsername=username.getText().toString();

                if (!mName.equals("") && !mPhone.equals("") && !mEmail.equals("") && isValidEmail(mEmail)) {
                    setUserInfo();
                }
                else if (!isValidEmail(mEmail)) {
                    Toast.makeText(CustomerSettings.this, "Enter a valid email id", Toast.LENGTH_SHORT).show();

                }
                else
                    Toast.makeText(CustomerSettings.this, "Fields cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
                return;
            }
        });

    }

    private String getUserInfo() {
        customerDatabaseRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists() && dataSnapshot.getChildrenCount()>0) {
                    Map<String, Object> map = (Map<String, Object>) dataSnapshot.getValue();
                    if (map.get("Name")!=null) {
                        mName = map.get("Name").toString();
                        name.setText(mName);
                    }
                    if (map.get("Phone")!=null) {
                        mPhone = map.get("Phone").toString();
                        phone.setText(mPhone);
                    }
                    if (map.get("Email")!=null) {
                        mEmail = map.get("Email").toString();
                        email.setText(mEmail );
                    }
                    if (map.get("Username")!=null) {
                        mUsername = map.get("Username").toString();
                        username.setText(mUsername);
                    }
                    if (map.get("Description")!=null) {
                        mDesc = map.get("Description").toString();
                        desc.setText(mDesc);
                    }
                    if (map.get("Profile Image Url")!=null) {
                        mProfileImageUrl = map.get("Profile Image Url").toString();
                        Glide.with(CustomerSettings.this).load(mProfileImageUrl).into(profileImg);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        return mEmail;
    }

    private void setUserInfo() {
        Map map = new HashMap();
        map.put("Name", mName);
        map.put("Phone", mPhone);
        map.put("Email", mEmail);
        map.put("Description", mDesc);
        map.put("Username", mUsername);

        customerDatabaseRef.updateChildren(map);


        if (resultUri!=null) {
            StorageReference filePath  = FirebaseStorage.getInstance().getReference("ProfileImages").child(user_id);
            Bitmap bitmap = null;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(CustomerSettings.this.getContentResolver(), resultUri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 20, byteArrayOutputStream);
            byte[] data = byteArrayOutputStream.toByteArray();
            UploadTask uploadTask = filePath.putBytes(data);

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(CustomerSettings.this, "Upload Failed", Toast.LENGTH_SHORT).show();
                    finFlag=1;
                }
            });

            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri downloadUrl = taskSnapshot.getUploadSessionUri();
                    Map newImg = new HashMap();
                    newImg.put("Profile Image Url", downloadUrl.toString());
                    customerDatabaseRef.updateChildren(newImg);
                    finFlag=1;
                    return;
                }
            });
        }
        else
            finFlag=1;

        if (!mEmail.equals(initEmail)) {
            finFlag=-1;
            mAuth.getCurrentUser().updateEmail(mEmail);
            mAuth.getCurrentUser().sendEmailVerification()
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(CustomerSettings.this, "Registered successfully. Please verify your email", Toast.LENGTH_LONG).show();
                            }
                            else
                                Toast.makeText(CustomerSettings.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }

        if (mAuth.getCurrentUser().isEmailVerified()) {
            finFlag=1;
        }
        else
            Toast.makeText(this, "Please verify your email address to save changes", Toast.LENGTH_SHORT).show();

        if (finFlag==1) finish();
    }


    public boolean isValidEmail(String email)
    {
        Pattern p = Pattern.compile(".+@.+\\.[a-z]+");
        Matcher m = p.matcher(email);
        return m.matches();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==1 && resultCode == AppCompatActivity.RESULT_OK) {
            final Uri imageUri = data.getData();

            resultUri = imageUri;
            profileImg.setImageURI(resultUri);
        }
    }
}
