package com.example.diaaldinkr.friendat2;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
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
    private Context context;

    public groupMessageAdapter(List<groupMessages> groupMessagesList, String groupID, Context context){
        this.groupMessagesList = groupMessagesList;
        this.groupID = groupID;
        this.context=context;
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
        final groupMessages groupMessages = groupMessagesList.get(i);
        final String fromUserID = groupMessages.getSender_id();
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
        } else if(fromMessageType.equals("image")){
            groupMessageViewHolder.receiverMessageText.setVisibility(View.GONE);
            groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
            if(fromUserID.equals(messageSenderID)){
                groupMessageViewHolder.receiverImageGroupMessages.setVisibility(View.GONE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.senderImageGroupMessages.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderImageGroupMessages.setBackgroundResource(R.drawable.sender_messages_layout);
                Picasso.get().load(groupMessages.getMessage()).placeholder(R.drawable.profile_image).into(groupMessageViewHolder.senderImageGroupMessages);
                groupMessageViewHolder.senderMessageTime.setText(groupMessages.getTime());
                groupMessageViewHolder.senderImageGroupMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageViewerIntent = new Intent(context.getApplicationContext(),ImageViewerActivity.class);
                        imageViewerIntent.putExtra("image",groupMessages.getMessage());
                        imageViewerIntent.putExtra("sender_id",fromUserID);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(imageViewerIntent);
                    }
                });
            } else {
                groupMessageViewHolder.senderImageGroupMessages.setVisibility(View.GONE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.receiverImageGroupMessages.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);

                groupMessageViewHolder.receiverImageGroupMessages.setBackgroundResource(R.drawable.receiver_messages_layout);
                Picasso.get().load(groupMessages.getMessage()).placeholder(R.drawable.profile_image).into(groupMessageViewHolder.receiverImageGroupMessages);
                groupMessageViewHolder.receiverMessageTime.setText(groupMessages.getTime());
                groupMessageViewHolder.receiverImageGroupMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageViewerIntent = new Intent(context.getApplicationContext(), ImageViewerActivity.class);
                        imageViewerIntent.putExtra("image", groupMessages.getMessage());
                        imageViewerIntent.putExtra("sender_id", fromUserID);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(imageViewerIntent);
                    }
                });
            }
        }

    }

    @Override
    public int getItemCount() {
        return groupMessagesList.size();
    }

    public class  groupMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime ,senderMessageTime ,senderMessageName ;
        public ImageButton receiverImageGroupMessages, senderImageGroupMessages;
        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_group_message_text);
            senderMessageName = itemView.findViewById(R.id.sender_group_message_name);
            senderMessageTime = itemView.findViewById(R.id.sender_group_message_time);
            senderImageGroupMessages = itemView.findViewById(R.id.sender_image_group_messages);

            receiverMessageText = itemView.findViewById(R.id.receiver_group_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_group_message_time);
            receiverImageGroupMessages = itemView.findViewById(R.id.receiver_image_group_messages);

            senderMessageText.setVisibility(View.GONE);
            senderMessageName.setVisibility(View.GONE);
            senderMessageTime.setVisibility(View.GONE);
            senderImageGroupMessages.setVisibility(View.GONE);

            receiverMessageText.setVisibility(View.GONE);
            receiverMessageTime.setVisibility(View.GONE);
            receiverImageGroupMessages.setVisibility(View.GONE);
        }
    }
}

