package com.example.diaaldinkr.friendat2;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.androidanimations.library.Techniques;
import com.daimajia.androidanimations.library.YoYo;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity {
    private  String messageReceiverID, messageReceiverName, messageReceiverLangCode, messageReceiverImage, messageSenderID;
    private TextView userName, lastSeen;
    private CircleImageView userImage;
    private Toolbar chatToolBar;
    private Button sendMessageButton;
    private EditText messageInput;
    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;
    private StorageReference usersImagesMessagesRef;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager linearLayoutManager;
    private MessageAdapter messageAdapter;
    private RecyclerView userMessagesList;
    private ImageButton attach;
    private  Uri resultUri;
    private Button recordButton;
    private boolean pick = false;

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


        sendMessageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float y=0;
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    y = event.getY();
                    Log.d("record", "Start Recording...");
                }else if (event.getAction() == MotionEvent.ACTION_UP) {
                    Log.d("record", "Stop Recording...");
                }else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    float c=event.getY();
                    if (c < y-50) {
                        Log.d("record", "delete Record...");
                    }
                }
                return true;
            }
        });

        rootRef.child("Messages").child(messageSenderID).child(messageReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        Log.d(">>>", "onChildAdded: "+ dataSnapshot.getValue());
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
        final boolean[] enter = {true};
        messageInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(">>>", "onTextChanged: "+s);
                if (messageInput.getText().toString().trim().length() > 0) {
                    if (enter[0]) {
                        YoYo.with(Techniques.FlipInY)
                                .duration(400)
                                .repeat(0)
                                .playOn(sendMessageButton);
                        enter[0] = false;
                    }
                    recordButton.setVisibility(View.GONE);
                    sendMessageButton.setVisibility(View.VISIBLE);
                } else if (messageInput.getText().toString().length() == 0) {
                    YoYo.with(Techniques.FlipInX)
                            .duration(400)
                            .repeat(0)
                            .playOn(recordButton);
                    recordButton.setVisibility(View.VISIBLE);
                    sendMessageButton.setVisibility(View.GONE);
                    enter[0] = true;
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        attach.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidVersion();
            }
        });
        String space="  ";
        Log.d("IMPORTANT", "SIZE: "+space.trim().length());
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

        //RESULT FROM CROPING ACTIVITY
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
        attach = findViewById(R.id.attach_button);
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
