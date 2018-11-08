package com.example.diaaldinkr.friendat2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class SettingsActivity extends AppCompatActivity {
    private Button updateAccountSettings;
    private EditText userName , userStatus;
    private CircleImageView userProfileImmage;

    private String currintUserID;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mAuth = FirebaseAuth.getInstance();
        currintUserID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();

        InitializeFields();

        userName.setVisibility(View.INVISIBLE);
        updateAccountSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                upDateSettings();
            }
        });

        retrieveUserInfo();

    }

    private void InitializeFields() {
        updateAccountSettings = findViewById(R.id.update_settings_button);
        userName = findViewById(R.id.set_user_name);
        userStatus = findViewById(R.id.set_profile_status);
        userProfileImmage = findViewById(R.id.set_profile_image);

    }

    private void upDateSettings() {

        String setUserName = userName.getText().toString();
        String setStatus = userStatus.getText().toString();

        if(TextUtils.isEmpty(setStatus)){
            Toast.makeText(this, "Please write your username ", Toast.LENGTH_SHORT).show();
        }
        if(TextUtils.isEmpty(setUserName)){
            Toast.makeText(this, "Please write your status ", Toast.LENGTH_SHORT).show();
        }
        else{
            //saving the data in the database
            HashMap<String,String> profileMap = new HashMap<>();
                profileMap.put("uid",currintUserID);
                profileMap.put("name",setUserName);
                profileMap.put("status",setStatus);
                //save the data in the user child
            rootRef.child("Users").child(currintUserID).setValue(profileMap)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                sendUserToMainActivity();
                                Toast.makeText(SettingsActivity.this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                            }
                            else{
                                String message= task.getException().toString();
                                Toast.makeText(SettingsActivity.this,"Error: "+ message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
    }

    private void retrieveUserInfo() {
        rootRef.child("Users").child(currintUserID)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        //if the user exist and he update the name and the status and the image
                        if(dataSnapshot.exists() && dataSnapshot.hasChild("name") && dataSnapshot.hasChild("image")){
                            //display the information
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus = dataSnapshot.child("status").getValue().toString();
                            String retriveProfileImage = dataSnapshot.child("image").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);

                        }
                        //if the user exist and he update the name and the status
                        else if(dataSnapshot.exists() && dataSnapshot.hasChild("name")){
                            String retriveUserName = dataSnapshot.child("name").getValue().toString();
                            String retriveStatus = dataSnapshot.child("status").getValue().toString();

                            userName.setText(retriveUserName);
                            userStatus.setText(retriveStatus);

                        }
                        //if the user is new or not update his information
                        else{
                            userName.setVisibility(View.VISIBLE);
                            Toast.makeText(SettingsActivity.this,"Please update your profile ", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(SettingsActivity.this, MainActivity.class);
        //to prevent the user from going back if he click on back button
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}
