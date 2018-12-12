package com.example.diaaldinkr.friendat2;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;

import de.hdodenhof.circleimageview.CircleImageView;

public class CreateGroupActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private DatabaseReference rootRef;
    private EditText groupNameField;
    private ProgressDialog loadingBar;
    private Button createGroup;
    private String groupName;

    private RecyclerView myContactList;

    private DatabaseReference contactsRef, usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;

    private ArrayList <String> groupUsersID;
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
        groupUsersID=new ArrayList<>();
        myContactList = findViewById(R.id.contacts_list);
        myContactList.setLayoutManager(new LinearLayoutManager(this));

        mAuth= FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        contactsRef = FirebaseDatabase.getInstance().getReference().child("Contacts").child(currentUserID);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");

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
                            rootRef.child("Groups").child(groupName).child("group_owner").setValue(currentUserID);
                            HashMap<String,Object> groupMembers = new HashMap<>();
                            for(int i=0;i<groupUsersID.size();i++){
                                groupMembers.put("user_ID"+i,groupUsersID.get(i));
                            }
                            rootRef.child("Groups").child(groupName).child("group_members").updateChildren(groupMembers);
                            //if the group add successfully
                            Toast.makeText(CreateGroupActivity.this,"The Group "+groupName+" is created successfully",Toast.LENGTH_SHORT).show();
                            loadingBar.dismiss();
                        }
                    }
                });
        for(int i=0;i<groupUsersID.size();i++){
            Log.d(">>>", groupUsersID.get(i));
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
                usersRef.child(userIDs).addValueEventListener(new ValueEventListener() {
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
                                    groupUsersID.remove(position);
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
