package com.example.diaaldinkr.friendat2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class CreateGroupActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference rootRef;
    private EditText groupNameField;
    private ProgressDialog loadingBar;
    private Button createGroup;
    private String groupName;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        InitializeFields();

        createGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                groupName = groupNameField.getText().toString();
                if(TextUtils.isEmpty(groupName)){
                    Toast.makeText(CreateGroupActivity.this,"Please enter the group name !",Toast.LENGTH_SHORT).show();
                }
                else{
                    //store the group in the database
                    createNewGroup(groupName);
                    groupNameField.setText("");
                    //i have to enter to the group directly from here
                    Intent groupChatIntent = new Intent(CreateGroupActivity.this,GroupChatActivity.class);
                    groupChatIntent.putExtra("groupName",groupName);
                    startActivity(groupChatIntent);
                }
            }
        });
    }

    private void InitializeFields() {
        rootRef= FirebaseDatabase.getInstance().getReference();
        groupNameField = findViewById(R.id.set_group_name);
        createGroup= findViewById(R.id.create_group_button);
        mToolbar = findViewById(R.id.create_group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("New group");
        loadingBar=new ProgressDialog(this);
    }


    private void createNewGroup(final String groupName) {
        loadingBar.setTitle(" Create Group ");
        loadingBar.setMessage("Please wait till creating the group end");
        //to stop the loading bar from appear if the user click on the screen
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        //In this method I store the group in the database
        rootRef.child("Groups").child(groupName).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            //if the group add successfully
                            Toast.makeText(CreateGroupActivity.this,"The Group "+groupName+" is created successfully",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }
}
