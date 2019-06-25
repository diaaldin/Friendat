
/* This class is to Send text messages or audio records or pictures in groups chat
 * Here I check the permissions for the camera and the microphone to take pictures and record audio
 * I save all the messages in the firebase database and save the images and the records in the firebase storage
 * and save the downloaded links in the firebase database
 * also i save the images and the records in the device storage
 * also checking the status of the users (online, offline or typing ) */

package com.abuTawfeek.diaaldinkr.friendat;

import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import de.hdodenhof.circleimageview.CircleImageView;

public class GroupChatActivity extends AppCompatActivity {

    private Toolbar groupToolBar;
    private Button sendMessageButton, recordButton;
    private EditText userMessageInput;
    private RecyclerView groupMessagesList;
    private TextView groupName;
    private CircleImageView groupImage;
    private TextView displayTextMessage;
    private FirebaseAuth mAuth;
    private FloatingActionButton sendImage ,sendVideo ,add;
    private DatabaseReference usersRef, groupIdRef, groupMessageKeyRef, groupMessageKeyRef2;
    private final List<groupMessages> messagesList = new ArrayList<>();
    private String currentGroupName, currentGroupImageURI, groupID, currentUserID, currentUserName , currentDate, currentTime;
    private groupMessageAdapter groupMessageAdapter;
    private LinearLayoutManager linearLayoutManager;
    private MediaRecorder mediaRecorder;
    private StorageReference groupsImagesMessagesRef;
    private StorageReference groupsVideoMessagesRef;
    private String pathSave;
    final int REQUEST_PERMISSION_CODE = 1000;
    final int PICK_VIDEO_CODE = 94;
    private boolean isFABOpen = false ;
    private boolean stopped = true ;
    private  Uri resultUri;
    private StorageReference usersAudiosMessagesRef;

    @SuppressLint("ClickableViewAccessibility")
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
        groupsImagesMessagesRef = FirebaseStorage.getInstance().getReference().child("Groups Images Messages");
        groupsVideoMessagesRef = FirebaseStorage.getInstance().getReference().child("Groups Video Messages");
        usersAudiosMessagesRef = FirebaseStorage.getInstance().getReference().child("Groups Audio Messages");
        InitializeFields();

        groupName.setText(currentGroupName);
        Picasso.get().load(currentGroupImageURI).placeholder(R.drawable.profile_image).into(groupImage);

        getUserInfo();
        userMessageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (userMessageInput.getText().toString().trim().length() > 0) {
//                    if (enter[0]) {
//                        YoYo.with(Techniques.FlipInY)
//                                .duration(400)
//                                .repeat(0)
//                                .playOn(sendMessageButton);
//                        enter[0] = false;
//                    }
                    recordButton.setVisibility(View.GONE);
                    sendMessageButton.setVisibility(View.VISIBLE);
                } else if (userMessageInput.getText().toString().length() == 0) {
//                    YoYo.with(Techniques.FlipInX)
//                            .duration(400)
//                            .repeat(0)
//                            .playOn(recordButton);
                    recordButton.setVisibility(View.VISIBLE);
                    sendMessageButton.setVisibility(View.GONE);
//                    enter[0] = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!isFABOpen){
                    showFABMenu();
                    isFABOpen=true;
                }else{
                    closeFABMenu();
                    isFABOpen=false;
                }
            }
        });
        add.performClick();
        /*
        Click listener for the record audio button
        */
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y=0;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    y = event.getY();
                    Toast.makeText(GroupChatActivity.this, "Start Recording...", Toast.LENGTH_SHORT).show();
                    if(checkPermissionFromDevice()){
                        setupMediaRecorder();
                        mediaRecorder.start();
                        stopped=false;
                    }else{
                        requestPermission();
                    }

                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (!stopped) {
                        mediaRecorder.stop();
                        stopped = true;
                        uploadAudio();
                    }
                }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float c=event.getY();
                    if (c < y-50) {
                        if (!stopped) {
                            mediaRecorder.stop();
                            mediaRecorder.reset();
                            stopped=true;
                        }
                    }
                }
                return true;
            }
        });
        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVideo();
            }
        });
        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidVersion();
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

    private void sendVideo() {
        startActivityForResult(Intent.createChooser(new Intent().
                        setAction(Intent.ACTION_GET_CONTENT).
                        setType("video/mp4"),
                "select video"),
                PICK_VIDEO_CODE);
    }

    private void showFABMenu(){

        add.animate().rotationBy(180).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {}

            @Override
            public void onAnimationEnd(Animator animator) {
                if (add.getRotation() != 180) {
                    add.setRotation(180);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {}

            @Override
            public void onAnimationRepeat(Animator animator) {}
        });
        sendImage.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        sendVideo.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu() {
        add.animate().rotationBy(-180);
        sendImage.animate().translationY(0);
        sendVideo.animate().translationY(0).setListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animator) {

            }

            @Override
            public void onAnimationEnd(Animator animator) {
                if (add.getRotation() != -180) {
                    add.setRotation(-180);
                }
            }

            @Override
            public void onAnimationCancel(Animator animator) {

            }

            @Override
            public void onAnimationRepeat(Animator animator) {

            }
        });
    }
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }
    private boolean checkPermissionFromDevice() {
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result== PackageManager.PERMISSION_GRANTED &&
                record_audio_result==PackageManager.PERMISSION_GRANTED;
    }

    private void uploadAudio() {
        Uri audioURI = Uri.fromFile(new File(pathSave));
        String messageKey  =groupIdRef.push().getKey();
        final DatabaseReference userMessageKeyRef = FirebaseDatabase.getInstance().getReference()
                .child("Groups").child(groupID)
                .child("user_messages").child(messageKey);

        //this key used to store the messages
        final String messagePushID = userMessageKeyRef.getKey();
        StorageReference filePath = usersAudiosMessagesRef.child(messagePushID + ".3gp");
        filePath.putFile(audioURI).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                if (task.isSuccessful()) {
                    //get the link of the profile image from the storage and store the link in the database
                    final String downloadUri = task.getResult().getDownloadUrl().toString();
                    String saveCurrentTime;
                    Calendar calendar = Calendar.getInstance();
                    SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                    saveCurrentTime = currentTime.format(calendar.getTime());
                    HashMap<String,Object> messageTextBody = new HashMap<>();
                    messageTextBody.put("name",currentUserName);
                    messageTextBody.put("sender_id",currentUserID);
                    messageTextBody.put("message",downloadUri);
                    messageTextBody.put("time",saveCurrentTime);
                    messageTextBody.put("type","audio");
                    userMessageKeyRef.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>() {
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
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(GroupChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    /*
       In this method I save the recorded audio in directory in the phone
   */
    private void setupMediaRecorder() {
        //make directory
        String sep = File.separator; // Use this instead of hardcoding the "/"
        String friendatFolder = "Friendat";
        String mediaFolder = "Media";
        String extStorageDirectory = Environment.getExternalStorageDirectory().toString();
        File myFriendatFolder = new File(extStorageDirectory + sep + friendatFolder);
        myFriendatFolder.mkdir();
        File myMediaFolder = new File(extStorageDirectory + sep + friendatFolder+ sep + mediaFolder);
        myMediaFolder.mkdir();
        pathSave = Environment.getExternalStorageDirectory().toString()
                + sep + friendatFolder + sep + mediaFolder + sep + UUID.randomUUID().toString()+ "_audio_record.3gp";
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setOutputFile(pathSave);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
        try {
            mediaRecorder.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void checkAndroidVersion(){
        //REQUEST PERMISSION
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            pickImage();
        } else {
            try {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 555);
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public void pickImage() {
        CropImage.startPickImageActivity(this);
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
            String encrypted = "";
            try {
                encrypted = AESUtils.encrypt(message);
                Log.d("TEST", "encrypted:" + encrypted);
            } catch (Exception e) {
                e.printStackTrace();
            }

            HashMap<String,Object> messageInfoMap = new HashMap<>();
            messageInfoMap.put("name",currentUserName);
            messageInfoMap.put("sender_id",currentUserID);
            //here i have to encrypt the message
            messageInfoMap.put("message",encrypted);
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
    }


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
        recordButton =  findViewById(R.id.record_button);
        userMessageInput = findViewById(R.id.input_group_message);
        groupMessagesList = findViewById(R.id.group_messages_list);
        groupName = findViewById(R.id.custom_group_name);
        groupImage = findViewById(R.id.custom_group_image);
        sendImage = findViewById(R.id.pick_image_button);
        sendVideo = findViewById(R.id.pick_video_button);
        add = findViewById(R.id.add);
        groupMessageAdapter = new groupMessageAdapter(messagesList, groupID, getApplicationContext());
        linearLayoutManager = new LinearLayoutManager(this);
        groupMessagesList.setLayoutManager(linearLayoutManager);

    }

    private void getUserInfo() {
        usersRef.child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
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
    //CROP REQUEST JAVA
    private void cropRequest(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setGuidelinesColor(R.color.colorPrimary)
                .setMultiTouchEnabled(true)
                .setCropMenuCropButtonIcon(R.drawable.ic_send)
                .setActivityTitle(currentGroupName)
                .setInitialCropWindowPaddingRatio(0)
                .start(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 555 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            checkAndroidVersion();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        //RESULT FROM SELECTED IMAGE
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri imageUri = CropImage.getPickImageResultUri(this, data);
            cropRequest(imageUri);
        }

        //RESULT FROM CROPPING ACTIVITY
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                resultUri = result.getUri();
                final String messageKey  =groupIdRef.push().getKey();
                groupMessageKeyRef2 = groupIdRef.child("user_messages").child(messageKey);
                DatabaseReference groupImageMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups").push();
                //this key used to store the messages
                final String messagePushID = groupImageMessageKeyRef.getKey();
                StorageReference filePath = groupsImagesMessagesRef.child(messagePushID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //get the link of the profile image from the storage and store the link in the database
                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            String saveCurrentTime = currentTime.format(calendar.getTime());
                            HashMap<String,Object> messageTextBody = new HashMap<>();
                            messageTextBody.put("name",currentUserName);
                            messageTextBody.put("sender_id",currentUserID);
                            messageTextBody.put("message",downloadUri);
                            messageTextBody.put("time",saveCurrentTime);
                            messageTextBody.put("type","image");
                            groupMessageKeyRef2.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    userMessageInput.setText("");
                                    add.performClick();
                                    if(task.isSuccessful()){
                                        Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                    }else{
                                        Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });

                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(GroupChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }
        if( requestCode == PICK_VIDEO_CODE && resultCode == Activity.RESULT_OK) {
            resultUri = data.getData();


            final String messageKey = groupIdRef.push().getKey();
            groupMessageKeyRef2 = groupIdRef.child("user_messages").child(messageKey);
            DatabaseReference groupVideoMessageKeyRef = FirebaseDatabase.getInstance().getReference().child("Groups").push();
            //this key used to store the messages
            final String messagePushID = groupVideoMessageKeyRef.getKey();
            StorageReference filePath = groupsVideoMessagesRef.child(messagePushID + ".mp4");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        //get the link of the profile image from the storage and store the link in the database
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                        String saveCurrentTime = currentTime.format(calendar.getTime());
                        HashMap<String, Object> messageTextBody = new HashMap<>();
                        messageTextBody.put("name", currentUserName);
                        messageTextBody.put("sender_id", currentUserID);
                        messageTextBody.put("message", downloadUri);
                        messageTextBody.put("time", saveCurrentTime);
                        messageTextBody.put("type", "video");
                        groupMessageKeyRef2.updateChildren(messageTextBody).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                userMessageInput.setText("");
                                add.performClick();
                                if (task.isSuccessful()) {
                                    Toast.makeText(GroupChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(GroupChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });

                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(GroupChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
}