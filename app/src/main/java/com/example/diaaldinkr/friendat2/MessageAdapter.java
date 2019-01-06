package com.example.diaaldinkr.friendat2;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.ExecutionException;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context context;
    public MessageAdapter(List<Messages> userMessagesList ,Context context){
        this.userMessagesList = userMessagesList;
        this.context=context;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.custom_messages_layout, viewGroup , false);
        mAuth = FirebaseAuth.getInstance();

        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder messageViewHolder, int i) {
        //retrieve and display the messages
        String messageSenderID = mAuth.getCurrentUser().getUid();
        Messages messages = userMessagesList.get(i);
        String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String messageTime = messages.getTime();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(fromUserID).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild("image")){
                            String receiverImage = dataSnapshot.child("image").getValue().toString();
                            Picasso.get().load(receiverImage).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverProfileImage);
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

        if(fromMessageType.equals("text")){
            String target_language=messages.getTo();
            Log.d(">>>", "onBindViewHolder: "+target_language);
            //Default variables for translation
            String textToBeTranslated = messages.getMessage();
            String TranslatedText;
            String source_language;
            source_language=Detect(textToBeTranslated);
            String languagePair = source_language+"-"+target_language; // ("<source_language>-<target_language>")
            //Executing the translation function
            TranslatedText=Translate(textToBeTranslated,languagePair);

            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverMessageText.setVisibility(View.GONE);
                messageViewHolder.receiverTranslatedMessageText.setVisibility(View.GONE);
                messageViewHolder.receiverMessageTime.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);

                messageViewHolder.senderMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderTranslatedMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);

                messageViewHolder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderTranslatedMessageText.setBackgroundResource(R.drawable.sender_translated_message_layout);
                messageViewHolder.senderMessageText.setText(messages.getMessage());
                messageViewHolder.senderTranslatedMessageText.setText(TranslatedText);
                messageViewHolder.senderMessageTime.setText(messages.getTime());
            }
            else{
                messageViewHolder.senderMessageText.setVisibility(View.GONE);
                messageViewHolder.senderTranslatedMessageText.setVisibility(View.GONE);
                messageViewHolder.senderMessageTime.setVisibility(View.GONE);

                messageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTranslatedMessageText.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverTranslatedMessageText.setBackgroundResource(R.drawable.receiver_translated_message_layout);

                messageViewHolder.receiverMessageText.setText(TranslatedText);
                messageViewHolder.receiverTranslatedMessageText.setText(messages.getMessage());
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
            }
        }else if(fromMessageType.equals("image")){

        }

    }
    //Function for calling executing the Detector Background Task
    private String Detect(String textToBeDetected){
        DetectorBackgroundTask detectorBackgroundTask= new DetectorBackgroundTask(context);
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = detectorBackgroundTask.execute(textToBeDetected).get();
            try {
                final JSONObject translationResultObj = new JSONObject(translationResult);
                translationResult=translationResultObj.get("text").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return translationResult;
    }
    //Function for calling executing the Translator Background Task
    private String Translate(String textToBeTranslated,String languagePair){
        TranslatorBackgroundTask translatorBackgroundTask= new TranslatorBackgroundTask(context);
        String translationResult = null; // Returns the translated text as a String
        try {
            translationResult = translatorBackgroundTask.execute(textToBeTranslated,languagePair).get();
            try {
                final JSONObject translationResultObj = new JSONObject(translationResult);
                translationResult=translationResultObj.get("text").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return translationResult;
    }
    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class  MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime, senderMessageTime ,senderTranslatedMessageText, receiverTranslatedMessageText;
        public CircleImageView receiverProfileImage;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderTranslatedMessageText = itemView.findViewById(R.id.sender_translated_message_text);
            senderMessageTime = itemView.findViewById(R.id.sender_message_time);


            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverTranslatedMessageText = itemView.findViewById(R.id.receiver_translated_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_message_time);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);

            senderMessageText.setVisibility(View.GONE);
            receiverMessageText.setVisibility(View.GONE);
            receiverProfileImage.setVisibility(View.GONE);
            senderTranslatedMessageText.setVisibility(View.GONE);
            receiverTranslatedMessageText.setVisibility(View.GONE);
        }
    }
}

