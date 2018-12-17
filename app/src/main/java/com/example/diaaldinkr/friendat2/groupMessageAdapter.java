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
    private List<groupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupRef;
    private String groupID;

    public groupMessageAdapter(List<groupMessages> groupMessagesList, String groupID){
        this.groupMessagesList = groupMessagesList;
        this.groupID = groupID;

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
        groupMessages groupMessages = groupMessagesList.get(i);
        String fromUserID = groupMessages.getSender_id();
        String fromUserName = groupMessages.getName();
        String fromMessageType = groupMessages.getType();
        String messageTime = groupMessages.getTime();

        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);

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
                groupMessageViewHolder.senderMessageText.setText(groupMessages.getMessage());
                groupMessageViewHolder.senderMessageTime.setText(groupMessages.getTime());
            }
            else{
                groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageName.setVisibility(View.VISIBLE);

                groupMessageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupMessageViewHolder.receiverMessageText.setText(groupMessages.getMessage());
                groupMessageViewHolder.senderMessageName.setText(groupMessages.getName());
                groupMessageViewHolder.receiverMessageTime.setText(groupMessages.getTime());
            }
        }

    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    public class  groupMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime ,senderMessageTime ,senderMessageName ;
        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_group_message_text);
            senderMessageName = itemView.findViewById(R.id.sender_group_message_name);
            senderMessageTime = itemView.findViewById(R.id.sender_group_message_time);

            receiverMessageText = itemView.findViewById(R.id.receiver_group_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_group_message_time);

            senderMessageText.setVisibility(View.GONE);
            senderMessageName.setVisibility(View.GONE);
            senderMessageTime.setVisibility(View.GONE);

            receiverMessageText.setVisibility(View.GONE);
            receiverMessageTime.setVisibility(View.GONE);
        }
    }
}

