
/*This class is used for create groups
* Save all the information of the group in the firebase storage
* Information like name, image and members of the group
*/

package com.example.diaaldinkr.friendat2;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
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
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;
import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private EditText groupNameField;
    private CircleImageView groupImage;
    private  static final  int galleryPick=1 ;
    private ProgressDialog loadingBar;
    private Button createGroup , addFriend;
    private String groupName;
    private RecyclerView myContactList;
    private DatabaseReference contactsRef, usersRef, rootRef, groupKeyRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private String groupPushID;
    private ArrayList <String> groupUsersID;
    private StorageReference groupImagesRef;
    private  Uri resultUri = Uri.parse("android.resource://com.example.diaaldinkr.friendat2/drawable/group_image");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);

        InitializeFields();
        groupImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAndroidVersion();
            }
        });
        addFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addFriend.setVisibility(View.GONE);
                myContactList.setVisibility(View.VISIBLE);
            }
        });
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
                    addFriend.setVisibility(View.VISIBLE);
                    myContactList.setVisibility(View.GONE);
                    //i have to enter to the group directly from here
                    Intent groupChatIntent = new Intent(CreateGroupActivity.this,GroupChatActivity.class);
                    groupChatIntent.putExtra("group_name",groupName);
                    groupChatIntent.putExtra("group_id",groupPushID);
                    groupChatIntent.putExtra("group_image", resultUri);
                    Log.d(">>>", "onClick: 11"+resultUri);
                    startActivity(groupChatIntent);
                }
            }
        });
    }

    private void InitializeFields() {
        groupUsersID=new ArrayList<>();
        myContactList = findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(this));
        groupImage = findViewById(R.id.set_group_image);
        mAuth= FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();
        groupUsersID.add(currentUserID);
        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupImagesRef = FirebaseStorage.getInstance().getReference().child("Group Images");
        rootRef= FirebaseDatabase.getInstance().getReference();
        groupNameField = findViewById(R.id.set_group_name);
        createGroup= findViewById(R.id.create_group_button);
        addFriend= findViewById(R.id.add_friend_group_button);
        mToolbar = findViewById(R.id.create_group_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        getSupportActionBar().setTitle("New group");
        loadingBar=new ProgressDialog(this);

        groupKeyRef= rootRef.child("Group").push();
    }


    private void createNewGroup(final String groupName) {
        loadingBar.setTitle(" Create Group ");
        loadingBar.setMessage("Please wait till creating the group end");
        //to stop the loading bar from appear if the user click on the screen
        loadingBar.setCanceledOnTouchOutside(true);
        loadingBar.show();
        groupPushID = groupKeyRef.getKey();
        //In this method I store the group in the database
        rootRef.child("Groups").child(groupPushID).setValue("")
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            rootRef.child("Groups").child(groupPushID).child("group_name").setValue(groupName);
                            rootRef.child("Groups").child(groupPushID).child("group_owner").setValue(currentUserID);
                            HashMap<String,Object> groupMembers = new HashMap<>();
                            groupMembers.put(currentUserID,true);
                            for(int i=1;i<groupUsersID.size();i++){
                                groupMembers.put(groupUsersID.get(i),true);
                            }
                            rootRef.child("Groups").child(groupPushID).child("group_members").updateChildren(groupMembers);
                            StorageReference filePath = groupImagesRef.child(groupPushID + ".jpg");
                            //store the image inside the firebase storage
                               filePath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                   @Override
                                   public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                       if (task.isSuccessful()) {
                                           //get the link of the profile image from the storage and store the link in the database
                                           final String downloadUri = task.getResult().getDownloadUrl().toString();
                                           rootRef.child("Groups").child(groupPushID).child("group_image").setValue(downloadUri);
                                       } else {
                                           String message = task.getException().toString();
                                           Toast.makeText(CreateGroupActivity.this, "Error : " + message, Toast.LENGTH_SHORT).show();
                                       }
                                   }
                               });

                            //if the group add successfully
                            Toast.makeText(CreateGroupActivity.this,"The Group "+groupName+" is created successfully",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
    }
    public void pickImage() {
        CropImage.startPickImageActivity(this);
    }

    //CROP REQUEST JAVA
    private void cropRequest(Uri imageUri) {
        CropImage.activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
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
                Picasso.get().load(resultUri).into(groupImage);
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

    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(contactsRef, Contacts.class)
                .build();

        FirebaseRecyclerAdapter<Contacts, CreateGroupActivity.addContactsViewHolder> adapter
                =new FirebaseRecyclerAdapter<Contacts, CreateGroupActivity.addContactsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final CreateGroupActivity.addContactsViewHolder holder, final int position, @NonNull final Contacts model) {
                final String userIDs = getRef(position).getKey();
                usersRef.child(userIDs).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()){
                            holder.onlineIcon.setVisibility(View.GONE);
                            if(dataSnapshot.hasChild("image")){
                                String userImage = dataSnapshot.child("image").getValue().toString();
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();

                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                                Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.profileImage);
                            }else {
                                String profileName = dataSnapshot.child("name").getValue().toString();
                                String profileStatus = dataSnapshot.child("status").getValue().toString();
                                //String userImage = dataSnapshot.child("image").getValue().toString();
                                holder.userName.setText(profileName);
                                holder.userStatus.setText(profileStatus);
                            }

                            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                                @Override
                                public boolean onLongClick(View v) {
                                    holder.itemView.setBackgroundColor(ContextCompat.getColor(CreateGroupActivity.this, R.color.list_item_selected_state));
                                    groupUsersID.add(userIDs);
                                    return true;
                                }
                            });
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    holder.itemView.setBackgroundColor(ContextCompat.getColor(CreateGroupActivity.this, R.color.list_item_normal_state));
                                    if(groupUsersID.size()>0 && groupUsersID.contains(userIDs))
                                        groupUsersID.remove(position);
                                    else
                                        Toast.makeText(CreateGroupActivity.this, "Add friends first -_- ", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public CreateGroupActivity.addContactsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.users_display_layout, viewGroup, false);
                CreateGroupActivity.addContactsViewHolder viewHolder = new CreateGroupActivity.addContactsViewHolder(view);
                return viewHolder;
            }
        };
        myContactList.setAdapter(adapter);
        adapter.startListening();
    }

    public static class addContactsViewHolder extends RecyclerView.ViewHolder {
        TextView userName, userStatus;
        CircleImageView profileImage;
        ImageView onlineIcon;

        public addContactsViewHolder(@NonNull View itemView) {
            super(itemView);
            userName = itemView.findViewById(R.id.user_profile_name);
            userStatus = itemView.findViewById(R.id.user_status);
            profileImage = itemView.findViewById(R.id.user_profile_image);
            onlineIcon = itemView.findViewById(R.id.user_online_status);
        }
    }

}
