package com.example.diaaldinkr.friendat2;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.mbms.MbmsErrors;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.support.v7.widget.Toolbar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar mToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private ScrollView mScrollView;
    private TextView displayTextMessage;

    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupNameRef, groupMessageKeyRef;

    private String currentGroupName, currentUserID, currentUserName , currentDate, currentTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("groupName").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupNameRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(currentGroupName);

        InitializeFields();
        
        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveMessage();
                userMessageInput.setText("");
            }
        });
    }

    private void saveMessage() {
        //in this method i store the messages in the database
        String message =userMessageInput.getText().toString();
        String messageKey  =groupNameRef.push().getKey();

        if(TextUtils.isEmpty(message)){
            Toast.makeText(this,"Please write message first",Toast.LENGTH_SHORT).show();
        }
        else{
            //save the messages in the database
            Calendar calForDate = Calendar.getInstance();
            SimpleDateFormat currentDateFormat= new SimpleDateFormat("MMM dd, yyyy");
            currentDate= currentDateFormat.format(calForDate.getTime());

            Calendar calForTime = Calendar.getInstance();
            SimpleDateFormat currentTimeFormat= new SimpleDateFormat("hh:mm a");
            currentTime= currentTimeFormat.format(calForTime.getTime());

            HashMap<String,Object> groupMessageKey = new HashMap<>();
            groupNameRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupNameRef.child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
                messageInfoMap.put("name",currentUserName);
                //here i have to encrypt the message
                messageInfoMap.put("message",message);
                messageInfoMap.put("date",currentDate);
                messageInfoMap.put("time",currentTime);
            groupMessageKeyRef.updateChildren(messageInfoMap);

        }
    }

    private void InitializeFields() {
        mToolBar = findViewById(R.id.group_chat_bar_layout);
        setSupportActionBar(mToolBar);
        getSupportActionBar().setTitle(currentGroupName);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        mScrollView = findViewById(R.id.my_scroll_view);
        displayTextMessage = findViewById(R.id.group_chat_text_display);
    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    //if the user exist then get his information
                    currentUserName= dataSnapshot.child("name").getValue().toString();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
