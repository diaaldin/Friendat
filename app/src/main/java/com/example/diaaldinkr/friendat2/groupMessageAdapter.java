package com.example.diaaldinkr.friendat2;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class groupMessageAdapter extends RecyclerView.Adapter<groupMessageAdapter.groupMessageViewHolder> {
    private List<Messages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupRef;

    public groupMessageAdapter(List<Messages> groupMessagesList ){
        this.groupMessagesList = groupMessagesList;
    }

    @NonNull
    @Override
    public groupMessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_group_message_layout, viewGroup , false);
        mAuth = FirebaseAuth.getInstance();

        return new groupMessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final groupMessageViewHolder groupMessageViewHolder, int i) {
        //retrieve and display the messages
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = groupMessagesList.get(i);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String messageTime = messages.getTime();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users").child(fromUserID);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        if(fromMessageType.equals("text")){
            /*messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
            messageViewHolder.senderMessageText.setVisibility(View.GONE);*/

            if(fromUserID.equals(messageSenderID)){
                groupMessageViewHolder.receiverMessageText.setVisibility(View.GONE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                groupMessageViewHolder.senderMessageText.setText(messages.getMessage());
                groupMessageViewHolder.senderMessageTime.setText(messages.getTime());
            }
            else{
                groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);

                groupMessageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupMessageViewHolder.receiverMessageText.setText(messages.getMessage());
                groupMessageViewHolder.receiverMessageTime.setText(messages.getTime());
            }
        }

    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    public class  groupMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime,senderMessageTime;
        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_group_message_text);
            senderMessageTime = itemView.findViewById(R.id.sender_group_message_time);


            receiverMessageText = itemView.findViewById(R.id.receiver_group_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_group_message_time);

            senderMessageText.setVisibility(View.GONE);
            receiverMessageText.setVisibility(View.GONE);
        }
    }
}

