package com.example.diaaldinkr.friendat2;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * A simple {@link Fragment} subclass.
 */
public class GroupsFragment extends Fragment {
    private View groupsView;
    private RecyclerView groupList;

    private DatabaseReference  usersRef;
    private FirebaseAuth mAuth;
    private String currentUserID;
    private DatabaseReference groupRef;
    private String groupPushID;

    public GroupsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        groupsView = inflater.inflate(R.layout.fragment_groups, container, false);
        mAuth=FirebaseAuth.getInstance();
        currentUserID = mAuth.getCurrentUser().getUid();

        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        groupList = groupsView.findViewById(R.id.groups_list);
        groupList.setLayoutManager(new LinearLayoutManager(getContext()));
        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups");
        return groupsView;

    }
    @Override
    public void onStart() {
        super.onStart();
        FirebaseRecyclerOptions<Contacts> options = new FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(groupRef, Contacts.class)
                .build();
        FirebaseRecyclerAdapter<Contacts, GroupsFragment.GroupsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contacts, GroupsFragment.GroupsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final GroupsFragment.GroupsViewHolder holder, int position, @NonNull Contacts model) {
                        final String[] groupImage = {"default_image"};
                        groupRef.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if(dataSnapshot.exists()){
                                    if(dataSnapshot.hasChild("group_image")){
                                        groupImage[0] = dataSnapshot.child("group_image").getValue().toString();
                                        Picasso.get().load(groupImage[0]).placeholder(R.drawable.profile_image).into(holder.groupImage);
                                    }
                                    String temp= dataSnapshot.child("/").getValue().toString();
                                    int i= temp.indexOf("=");
                                    groupPushID = temp.substring(1,i);
                                    final String groupName = dataSnapshot.child(groupPushID).child("group_name").getValue().toString();
                                    Log.d(">>>", "onCreateView: "+groupName);
                                    holder.groupName.setText(groupName);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            Intent groupIntent = new Intent(getContext(),GroupChatActivity.class);
                                            groupIntent.putExtra("group_name",groupName);
                                            groupIntent.putExtra("group_id",groupPushID);
                                            groupIntent.putExtra("group_image",groupImage[0]);
                                            startActivity(groupIntent);
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
                    public GroupsFragment.GroupsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.group_display_layout, viewGroup, false);
                        GroupsFragment.GroupsViewHolder viewHolder = new GroupsFragment.GroupsViewHolder(view);
                        return viewHolder;
                    }
                };
        groupList.setAdapter(adapter);
        adapter.startListening();


    }
    public static class GroupsViewHolder extends RecyclerView.ViewHolder {
        TextView groupName;
        CircleImageView groupImage;

        public GroupsViewHolder(@NonNull View itemView) {
            super(itemView);
            groupName = itemView.findViewById(R.id.group_name);
            groupImage = itemView.findViewById(R.id.group_image);
        }
    }
}