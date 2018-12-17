package com.example.diaaldinkr.friendat2;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar groupToolBar;
    private ImageButton sendMessageButton;
    private EditText userMessageInput;
    private RecyclerView groupMessagesList;
    private TextView groupName;
    private CircleImageView groupImage;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private DatabaseReference usersRef, groupIdRef, groupMessageKeyRef;
    private final List<groupMessages> messagesList = new ArrayList<>();
    private String currentGroupName, currentGroupImageURI, groupID, currentUserID, currentUserName , currentDate, currentTime;
    private groupMessageAdapter groupMessageAdapter;
    private LinearLayoutManager linearLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_chat);

        currentGroupName=getIntent().getExtras().get("group_name").toString();
        groupID=getIntent().getExtras().get("group_id").toString();
        currentGroupImageURI=getIntent().getExtras().get("group_image").toString();

        mAuth = FirebaseAuth.getInstance();
        currentUserID= mAuth.getCurrentUser().getUid();
        usersRef= FirebaseDatabase.getInstance().getReference().child("Users");
        groupIdRef=FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);

        InitializeFields();

        groupName.setText(currentGroupName);
        Picasso.get().load(currentGroupImageURI).placeholder(R.drawable.profile_image).into(groupImage);

        getUserInfo();

        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        groupIdRef.child("user_messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    groupMessages messages = dataSnapshot.getValue(groupMessages.class);
                    messagesList.add(messages);
                    groupMessageAdapter.notifyDataSetChanged();
//                  userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                    groupMessagesList.setAdapter(groupMessageAdapter);
                }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage() {
        //in this method i store the messages in the database
        String message =userMessageInput.getText().toString();
        String messageKey  =groupIdRef.push().getKey();

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
            groupIdRef.updateChildren(groupMessageKey);

            groupMessageKeyRef = groupIdRef.child("user_messages").child(messageKey);

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("sender_id",currentUserID);
            //here i have to encrypt the message
            messageInfoMap.put("message",message);
            messageInfoMap.put("time",currentTime);
            messageInfoMap.put("type","text");
            groupMessageKeyRef.updateChildren(messageInfoMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    userMessageInput.setText("");
                    if(task.isSuccessful()){
                        Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        linearLayoutManager.setStackFromEnd(true);
        /*groupIdRef.child("user_messages").addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    //if the group exists and display the messages
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                if(dataSnapshot.exists()){
                    //if the group exists and display the messages
                    DisplayMessages(dataSnapshot);
                }
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });*/
    }

    /*private void DisplayMessages(DataSnapshot dataSnapshot) {
//retrieve and display all the messages
        Iterator iter = dataSnapshot.getChildren().iterator();
        while (iter.hasNext()){
            String chatDate = (String)((DataSnapshot)iter.next()).getValue();
            //Here i had to decrypt the message
            String chatMessage = (String)((DataSnapshot)iter.next()).getValue();
            String chatName = (String)((DataSnapshot)iter.next()).getValue();
            String chatTime = (String)((DataSnapshot)iter.next()).getValue();

            displayTextMessage.append(chatName + ": \n"+ chatMessage + "\n"+chatTime+"      "+chatDate+"\n\n\n");

        }
    }*/

    private void InitializeFields() {
        groupToolBar = findViewById(R.id.group_toolbar);
        setSupportActionBar(groupToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_group_bar, null);
        actionBar.setCustomView(actionBarView);

        sendMessageButton = findViewById(R.id.send_message_button);
        userMessageInput = findViewById(R.id.input_group_message);
        groupMessagesList = findViewById(R.id.group_messages_list);
        groupName = findViewById(R.id.custom_group_name);
        groupImage = findViewById(R.id.custom_group_image);
        groupMessageAdapter = new groupMessageAdapter(messagesList,groupID);
        linearLayoutManager = new LinearLayoutManager(this);
        groupMessagesList.setLayoutManager(linearLayoutManager);

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