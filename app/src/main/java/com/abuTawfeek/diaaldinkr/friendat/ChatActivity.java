

/* This class is to Send text messages or audio records or pictures in single chat
 * Here I check the permissions for the camera and the microphone to take pictures and record audio
 * I save all the messages in the firebase database and save the images and the records in the firebase storage
 * and save the downloaded links in the firebase database
 * also i save the images and the records in the device storage
 * also checking the status of the users (online, offline or typing ) */





package com.abuTawfeek.diaaldinkr.friendat;

/*
Imports
*/
import android.Manifest;
import android.animation.Animator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
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

public class ChatActivity extends AppCompatActivity {
    /*
    Variables
    */
    private  String messageReceiverID, messageReceiverName, messageReceiverLangCode, messageReceiverImage, messageSenderID;
    private TextView userName, lastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private Button sendMessageButton, recordButton;
    private EditText messageInput;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference usersImagesMessagesRef;
    private StorageReference usersAudiosMessagesRef;
    private StorageReference usersVideosMessagesRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private FloatingActionButton sendImage ,sendVideo ,add;
    private  Uri resultUri;
    private boolean pick = false;
    private MediaRecorder mediaRecorder;
    private String pathSave;
    final int REQUEST_PERMISSION_CODE = 1000;
    final int PICK_VIDEO_CODE = 94;
    private MediaPlayer mediaPlayer;
    private boolean stopped =true;
    private boolean isFABOpen=false ;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mAuth = FirebaseAuth.getInstance();
        messageSenderID = mAuth.getCurrentUser().getUid();
        rootRef = FirebaseDatabase.getInstance().getReference();
        messageReceiverID = getIntent().getExtras().get("visit_user_id").toString();
        messageReceiverName = getIntent().getExtras().get("visit_user_name").toString();
        messageReceiverImage = getIntent().getExtras().get("visit_user_image").toString();
        messageReceiverLangCode = getIntent().getExtras().get("visit_user_lang_code").toString();



        initializeControllers();
        userName.setText(messageReceiverName);
        Picasso.get().load(messageReceiverImage).placeholder(R.drawable.profile_image).into(userImage);
        usersImagesMessagesRef = FirebaseStorage.getInstance().getReference().child("Images Messages");
        usersAudiosMessagesRef = FirebaseStorage.getInstance().getReference().child("Audio Messages");
        usersVideosMessagesRef = FirebaseStorage.getInstance().getReference().child("Video Messages");
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
        Click listener for the send message button
        */
        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        sendVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendVideo();
            }
        });
        /*
        Click listener for the record audio button
        */
        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y=0;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    y = event.getY();
                    Toast.makeText(ChatActivity.this, "Start Recording...", Toast.LENGTH_SHORT).show();
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
        /*
        Retrieve the messages from the database
        */
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Messages messages = dataSnapshot.getValue(Messages.class);
                        messagesList.add(messages);
                        messageAdapter.notifyDataSetChanged();
//                        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());
                        userMessagesList.setAdapter(messageAdapter);
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
        /*
        Check if the user start to type message
        */
        final boolean[] enter = {true};
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (messageInput.getText().toString().trim().length() > 0) {
//                    if (enter[0]) {
//                        YoYo.with(Techniques.FlipInY)
//                                .duration(400)
//                                .repeat(0)
//                                .playOn(sendMessageButton);
//                        enter[0] = false;
//                    }
                    recordButton.setVisibility(View.GONE);
                    sendMessageButton.setVisibility(View.VISIBLE);
                } else if (messageInput.getText().toString().length() == 0) {
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

        sendImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidVersion();
            }
        });
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

    private void sendVideo() {
        startActivityForResult(Intent.createChooser(new Intent().
                        setAction(Intent.ACTION_GET_CONTENT).
                        setType("video/mp4"),
                "select video"),
                PICK_VIDEO_CODE);
    }

    /*
        In this method I upload the recorded audio to the firebase storage and save the link in the firebase database
    */
    private void uploadAudio() {
        Uri audioURI = Uri.fromFile(new File(pathSave));
        DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                .child(messageReceiverID).push();

        final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
        final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;
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
                    Map messageTextBody = new HashMap();

                    messageTextBody.put("message", downloadUri);
                    //this is the message type and the text for just text messages i had to add another types
                    messageTextBody.put("type", "audio");
                    messageTextBody.put("from", messageSenderID);
                    messageTextBody.put("time", saveCurrentTime);

                    Map messageBodyDetails = new HashMap();
                    messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                    messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                    rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    String message = task.getException().toString();
                    Toast.makeText(ChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
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
    /*
      In this method to request permission from the user to record audio and use external storage
    */
    private void requestPermission() {
        ActivityCompat.requestPermissions(this,new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.RECORD_AUDIO
        },REQUEST_PERMISSION_CODE);
    }


    private boolean checkPermissionFromDevice() {
        int record_audio_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        int write_external_storage_result = ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO);
        return write_external_storage_result==PackageManager.PERMISSION_GRANTED &&
                record_audio_result==PackageManager.PERMISSION_GRANTED;
    }

    private void checkAndroidVersion(){
        //REQUEST PERMISSION
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, 555);
            }catch (Exception e){

            }
        } else {
            pickImage();
        }
    }
    public void pickImage() {
        CropImage.startPickImageActivity(this);
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
                //store the image inside the firebase storage
                final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
                final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

                DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                        .child(messageReceiverID).push();
                //this key used to store the messages
                final String messagePushID = userMessageKeyRef.getKey();
                StorageReference filePath = usersImagesMessagesRef.child(messagePushID + ".jpg");
                filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {
                            //get the link of the profile image from the storage and store the link in the database
                            final String downloadUri = task.getResult().getDownloadUrl().toString();
                            String saveCurrentTime;
                            Calendar calendar = Calendar.getInstance();
                            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                            saveCurrentTime = currentTime.format(calendar.getTime());
                            Map messageTextBody = new HashMap();
                            /**************************************************************************************/
                            /**************************************************************************************/
                            messageTextBody.put("message", downloadUri);
                            //this is the message type and the text for just text messages i had to add another types
                            messageTextBody.put("type", "image");
                            messageTextBody.put("from", messageSenderID);
                            messageTextBody.put("time", saveCurrentTime);

                            Map messageBodyDetails = new HashMap();
                            messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                            messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                                @Override
                                public void onComplete(@NonNull Task task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                    } else {
                                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        } else {
                            String message = task.getException().toString();
                            Toast.makeText(ChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }

        if( requestCode == PICK_VIDEO_CODE && resultCode == Activity.RESULT_OK){
            resultUri = data.getData();
            //store the image inside the firebase storage
            final String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            final String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            //this key used to store the messages
            final String messagePushID = userMessageKeyRef.getKey();
            StorageReference filePath = usersVideosMessagesRef.child(messagePushID + ".mp4");
            filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                    if (task.isSuccessful()) {
                        //get the link of the profile image from the storage and store the link in the database
                        final String downloadUri = task.getResult().getDownloadUrl().toString();
                        String saveCurrentTime;
                        Calendar calendar = Calendar.getInstance();
                        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
                        saveCurrentTime = currentTime.format(calendar.getTime());
                        Map messageTextBody = new HashMap();
                        /**************************************************************************************/
                        /**************************************************************************************/
                        messageTextBody.put("message", downloadUri);
                        //this is the message type and the text for just text messages i had to add another types
                        messageTextBody.put("type", "video");
                        messageTextBody.put("from", messageSenderID);
                        messageTextBody.put("time", saveCurrentTime);

                        Map messageBodyDetails = new HashMap();
                        messageBodyDetails.put(messageSenderRef + "/" + messagePushID, messageTextBody);
                        messageBodyDetails.put(messageReceiverRef + "/" + messagePushID, messageTextBody);

                        rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                            @Override
                            public void onComplete(@NonNull Task task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    } else {
                        String message = task.getException().toString();
                        Toast.makeText(ChatActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 555 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            pickImage();
        } else {
            checkAndroidVersion();
        }
    }
    //CROP REQUEST JAVA
    private void cropRequest(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setGuidelinesColor(R.color.colorPrimary)
                .setMultiTouchEnabled(true)
                .setCropMenuCropButtonIcon(R.drawable.ic_send)
                .setActivityTitle(messageReceiverName)
                .setInitialCropWindowPaddingRatio(0)
                .start(this);
    }
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if(TextUtils.isEmpty(messageText)){
            Toast.makeText(this, "write a message first", Toast.LENGTH_SHORT).show();
        }else{
            messageInput.setText("");
            String messageSenderRef = "Messages/" + messageSenderID + "/" + messageReceiverID;
            String messageReceiverRef = "Messages/" + messageReceiverID + "/" + messageSenderID;

            DatabaseReference userMessageKeyRef = rootRef.child("Messages").child(messageSenderID)
                    .child(messageReceiverID).push();
            //this key used to store the messages
            String messagePushID = userMessageKeyRef.getKey();

            String saveCurrentTime;
            Calendar calendar = Calendar.getInstance();
            SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
            saveCurrentTime = currentTime.format(calendar.getTime());
            Map messageTextBody = new HashMap();
            /**************************************************************************************/
            /**************************************************************************************/
            messageTextBody.put("message",messageText);
            //this is the message type and the text for just text messages i had to add another types
            messageTextBody.put("type","text");
            messageTextBody.put("from",messageSenderID);
            messageTextBody.put("to",messageReceiverLangCode);
            messageTextBody.put("time",saveCurrentTime);

            Map messageBodyDetails= new HashMap();
            messageBodyDetails.put(messageSenderRef + "/"+ messagePushID , messageTextBody);
            messageBodyDetails.put(messageReceiverRef + "/"+ messagePushID , messageTextBody);

            rootRef.updateChildren(messageBodyDetails).addOnCompleteListener(new OnCompleteListener() {
                @Override
                public void onComplete(@NonNull Task task) {
                    if(task.isSuccessful()){
                        Toast.makeText(ChatActivity.this, "message sent", Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(ChatActivity.this, "Error", Toast.LENGTH_SHORT).show();
                    }
                }
            });

        }
    }
    private void initializeControllers() {
        //customize the toolbar
        chatToolBar = findViewById(R.id.chat_toolbar);
        setSupportActionBar(chatToolBar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayShowCustomEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        LayoutInflater layoutInflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View actionBarView = layoutInflater.inflate(R.layout.custom_chat_bar, null);
        actionBar.setCustomView(actionBarView);
        //initialize the fields
        userImage = findViewById(R.id.custom_profile_image);
        userName = findViewById(R.id.custom_profile_name);
        lastSeen = findViewById(R.id.custom_last_seen);
        add = findViewById(R.id.add);
        sendImage = findViewById(R.id.pick_image_button);
        sendVideo = findViewById(R.id.pick_video_button);
        messageInput = findViewById(R.id.input_message);
        sendMessageButton = findViewById(R.id.send_message_btn);
        recordButton =  findViewById(R.id.record_button);

        userMessagesList = findViewById(R.id.private_messages_list);
        messageAdapter = new MessageAdapter(messagesList, getApplicationContext());
        linearLayoutManager = new LinearLayoutManager(this);
        userMessagesList.setLayoutManager(linearLayoutManager);
    }

    @Override
    protected void onStart() {
        super.onStart();
        displayLastSeen();
        linearLayoutManager.setStackFromEnd(true);
//        userMessagesList.smoothScrollToPosition(userMessagesList.getAdapter().getItemCount());

    }

    private void displayLastSeen(){
        rootRef.child("Users").child(messageSenderID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //retrieve the last seen, the date and the time
                if(dataSnapshot.child("user_state").hasChild("state")){
                    String state = dataSnapshot.child("user_state").child("state").getValue().toString();
                    String date = dataSnapshot.child("user_state").child("date").getValue().toString();
                    String time = dataSnapshot.child("user_state").child("time").getValue().toString();

                    if(state.equals("online")){
                        lastSeen.setText("online");
                    }else if(state.equals("offline")){
                        lastSeen.setText("Last Seen: "+date+ " "+ time);
                    }

                }else{
                    lastSeen.setText("offline");
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

}
