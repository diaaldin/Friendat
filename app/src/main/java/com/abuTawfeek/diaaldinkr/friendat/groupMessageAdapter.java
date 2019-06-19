package com.abuTawfeek.diaaldinkr.friendat;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class groupMessageAdapter extends RecyclerView.Adapter<groupMessageAdapter.groupMessageViewHolder> {
    private List<groupMessages> groupMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef, groupRef;
    private String groupID;
    private Context context;
    private boolean entered = true;
    private boolean isPlaying = false;
    private String lang = "en";
    private boolean isLangInit=false;

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
        final String messageSenderID = mAuth.getCurrentUser().getUid();
        final groupMessages groupMessages = groupMessagesList.get(i);
        final String fromUserID = groupMessages.getSender_id();
        String fromUserName = groupMessages.getName();
        String fromMessageType = groupMessages.getType();
        String messageTime = groupMessages.getTime();

        groupRef = FirebaseDatabase.getInstance().getReference().child("Groups").child(groupID);

        if(fromMessageType.equals("text")){
            String id = mAuth.getCurrentUser().getUid();
            userRef = FirebaseDatabase.getInstance().getReference();
            userRef.child("Users").child(id).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    lang = dataSnapshot.toString();
                    if(lang.contains("lang_code")){
                        lang = lang.substring(lang.indexOf("lang_code"));
                        lang = lang.substring(lang.indexOf("=")+1, lang.indexOf(","));
                        isLangInit=true;

                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });

            if(!isLangInit){
                lang="en";
            }

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
                String target_language = lang;
                // Default variables for translation
                String textToBeTranslated = groupMessages.getMessage();
                String TranslatedText;
                String source_language;
                source_language=Detect(textToBeTranslated);
                String languagePair = source_language+"-"+target_language; // ("<source_language>-<target_language>")
                // Executing the translation function
                TranslatedText=Translate(textToBeTranslated,languagePair);
                groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.receiverMessageText.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageName.setVisibility(View.VISIBLE);

                groupMessageViewHolder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupMessageViewHolder.receiverMessageText.setText(TranslatedText);
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
                groupMessageViewHolder.senderMessageName.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderMessageName.setText(groupMessages.getName());
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
        } else if(fromMessageType.equals("audio")){
            final MediaPlayer mediaPlayer = new MediaPlayer();
            Uri myUri = Uri.parse(groupMessages.getMessage()); // initialize Uri here
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(context, myUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }

            groupMessageViewHolder.receiverMessageText.setVisibility(View.GONE);
            groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
            groupMessageViewHolder.senderImageGroupMessages.setVisibility(View.GONE);
            groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);
            groupMessageViewHolder.receiverImageGroupMessages.setVisibility(View.GONE);
            groupMessageViewHolder.receiverMessageTime.setVisibility(View.GONE);
            if(fromUserID.equals(messageSenderID)){
                groupMessageViewHolder.receiverGroupPlayTime.setVisibility(View.GONE);
                groupMessageViewHolder.receiverGroupPlayRecord.setVisibility(View.GONE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.GONE);

                groupMessageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderGroupPlayTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderGroupPlayRecord.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderGroupPlayRecord.setBackgroundResource(R.drawable.sender_messages_layout);
                groupMessageViewHolder.senderMessageTime.setText(groupMessages.getTime());
                groupMessageViewHolder.senderGroupPlayTime.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                groupMessageViewHolder.senderGroupPlayRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPlaying){
                            groupMessageViewHolder.senderGroupPlayRecord.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                        }else{
                            groupMessageViewHolder.senderGroupPlayRecord.setImageResource(R.drawable.ic_pause_record);
                            mediaPlayer.start();
                        }
                        isPlaying =!isPlaying;

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.seekTo(0);
                        groupMessageViewHolder.senderGroupPlayRecord.setImageResource(R.drawable.ic_play);
                        isPlaying =!isPlaying;
                    }
                });
            }else{
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);
                groupMessageViewHolder.senderGroupPlayRecord.setVisibility(View.GONE);
                groupMessageViewHolder.senderGroupPlayTime.setVisibility(View.GONE);

                groupMessageViewHolder.receiverGroupPlayRecord.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverGroupPlayTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderMessageName.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderMessageName.setText(groupMessages.getName());
                groupMessageViewHolder.receiverGroupPlayRecord.setBackgroundResource(R.drawable.receiver_messages_layout);
                groupMessageViewHolder.receiverGroupPlayTime.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                groupMessageViewHolder.receiverMessageTime.setText(groupMessages.getTime());
                groupMessageViewHolder.receiverGroupPlayRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPlaying){
                            groupMessageViewHolder.receiverGroupPlayRecord.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                        }else{
                            groupMessageViewHolder.receiverGroupPlayRecord.setImageResource(R.drawable.ic_pause_record);
                            mediaPlayer.start();
                        }
                        isPlaying =!isPlaying;

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.seekTo(0);
                        groupMessageViewHolder.receiverGroupPlayRecord.setImageResource(R.drawable.ic_play);
                        isPlaying =!isPlaying;
                    }
                });
            }
        } else if(fromMessageType.equals("video")){
            groupMessageViewHolder.senderMessageText.setVisibility(View.GONE);
            groupMessageViewHolder.playVideoGroup.setVisibility(View.GONE);
            groupMessageViewHolder.senderImageGroupMessages.setVisibility(View.GONE);
            groupMessageViewHolder.senderGroupPlayTime.setVisibility(View.GONE);
            groupMessageViewHolder.senderGroupPlayRecord.setVisibility(View.GONE);

            groupMessageViewHolder.receiverMessageText.setVisibility(View.GONE);
            groupMessageViewHolder.receiverImageGroupMessages.setVisibility(View.GONE);
            groupMessageViewHolder.receiverGroupPlayTime.setVisibility(View.GONE);
            groupMessageViewHolder.receiverGroupPlayRecord.setVisibility(View.GONE);
            groupMessageViewHolder.receiverGroupVideoMessage.setVisibility(View.GONE);

            if(fromUserID.equals(messageSenderID)){
                groupMessageViewHolder.receiverMessageTime.setVisibility(View.GONE);
                groupMessageViewHolder.receiverGroupVideoMessage.setVisibility(View.GONE);
                groupMessageViewHolder.playVideoGroup.setVisibility(View.GONE);
                groupMessageViewHolder.frameLayoutGroup.setVisibility(View.GONE);

                groupMessageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.senderGroupVideoMessage.setVisibility(View.VISIBLE);
                groupMessageViewHolder.playVideo2Group.setVisibility(View.VISIBLE);
                groupMessageViewHolder.frameLayout2Group.setVisibility(View.VISIBLE);

                groupMessageViewHolder.senderMessageTime.setText(groupMessages.getTime());
                Uri myUri = Uri.parse(groupMessages.getMessage()); // initialize Uri here
                groupMessageViewHolder.senderGroupVideoMessage.setVideoURI(myUri);
                groupMessageViewHolder.senderGroupVideoMessage.seekTo( 1 );

                final Intent videoViewerIntent = new Intent(context.getApplicationContext(),VideoViewer.class);
                videoViewerIntent.putExtra("video",groupMessages.getMessage());
                videoViewerIntent.putExtra("sender_id",fromUserID);
                videoViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                groupMessageViewHolder.senderGroupVideoMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
                groupMessageViewHolder.playVideo2Group.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
            }else{
                groupMessageViewHolder.senderMessageTime.setVisibility(View.GONE);
                groupMessageViewHolder.senderGroupVideoMessage.setVisibility(View.GONE);
                groupMessageViewHolder.playVideo2Group.setVisibility(View.GONE);
                groupMessageViewHolder.frameLayout2Group.setVisibility(View.GONE);

                groupMessageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                groupMessageViewHolder.receiverGroupVideoMessage.setVisibility(View.VISIBLE);
                groupMessageViewHolder.playVideoGroup.setVisibility(View.VISIBLE);
                groupMessageViewHolder.frameLayoutGroup.setVisibility(View.VISIBLE);

                groupMessageViewHolder.receiverMessageTime.setText(groupMessages.getTime());
                Uri myUri = Uri.parse(groupMessages.getMessage()); // initialize Uri here
                groupMessageViewHolder.receiverGroupVideoMessage.setVideoURI(myUri);
                groupMessageViewHolder.receiverGroupVideoMessage.seekTo( 1 );
                final Intent videoViewerIntent = new Intent(context.getApplicationContext(),VideoViewer.class);
                videoViewerIntent.putExtra("video",groupMessages.getMessage());
                videoViewerIntent.putExtra("sender_id",fromUserID);
                videoViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                groupMessageViewHolder.receiverGroupVideoMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
                groupMessageViewHolder.playVideoGroup.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
            }
        }
    }

    public  String milliSecondsToTimer(long milliseconds) {
        String finalTimerString = "";
        String secondsString = "";
        // Convert total duration into time
        int hours = (int) (milliseconds / (1000 * 60 * 60));
        int minutes = (int) (milliseconds % (1000 * 60 * 60)) / (1000 * 60);
        int seconds = (int) ((milliseconds % (1000 * 60 * 60)) % (1000 * 60) / 1000);
        // Add hours if there
        if (hours > 0) {
            finalTimerString = hours + ":";
        }
        // Prepending 0 to seconds if it is one digit
        if (seconds < 10) {
            secondsString = "0" + seconds;
        } else {
            secondsString = "" + seconds;
        }
        finalTimerString = finalTimerString + minutes + ":" + secondsString;
        // return timer string
        return finalTimerString;
    }
    private void getTo() {

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
        return groupMessagesList.size();
    }

    public class  groupMessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime ,senderMessageTime ,senderMessageName ,senderGroupPlayTime, receiverGroupPlayTime ;
        public ImageButton receiverImageGroupMessages, senderImageGroupMessages, playVideoGroup, playVideo2Group;
        public ImageView senderGroupPlayRecord, receiverGroupPlayRecord;
        public VideoView senderGroupVideoMessage, receiverGroupVideoMessage;
        public FrameLayout frameLayoutGroup, frameLayout2Group;

        public groupMessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_group_message_text);
            senderMessageName = itemView.findViewById(R.id.sender_group_message_name);
            senderMessageTime = itemView.findViewById(R.id.sender_group_message_time);
            senderImageGroupMessages = itemView.findViewById(R.id.sender_image_group_messages);
            senderGroupPlayTime = itemView.findViewById(R.id.sender_group_record_time);
            senderGroupPlayRecord = itemView.findViewById(R.id.sender_group_play_record);
            playVideoGroup = itemView.findViewById(R.id.play_video_group);
            frameLayoutGroup = itemView.findViewById(R.id.video_frame_group);
            senderGroupVideoMessage = itemView.findViewById(R.id.sender_group_video_layout);

            receiverMessageText = itemView.findViewById(R.id.receiver_group_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_group_message_time);
            receiverImageGroupMessages = itemView.findViewById(R.id.receiver_image_group_messages);
            receiverImageGroupMessages = itemView.findViewById(R.id.receiver_image_group_messages);
            receiverGroupPlayTime = itemView.findViewById(R.id.receiver_group_record_time);
            receiverGroupPlayRecord = itemView.findViewById(R.id.receiver_group_play_record);
            playVideo2Group = itemView.findViewById(R.id.play_video2_group);
            frameLayout2Group = itemView.findViewById(R.id.video_frame2_group);
            receiverGroupVideoMessage = itemView.findViewById(R.id.receiver_group_video_layout);

            senderMessageText.setVisibility(View.GONE);
            senderMessageName.setVisibility(View.GONE);
            senderMessageTime.setVisibility(View.GONE);
            senderImageGroupMessages.setVisibility(View.GONE);
            senderGroupPlayTime.setVisibility(View.GONE);
            senderGroupPlayRecord.setVisibility(View.GONE);
            playVideoGroup.setVisibility(View.GONE);
            frameLayoutGroup.setVisibility(View.GONE);
            senderGroupVideoMessage.setVisibility(View.GONE);

            receiverMessageText.setVisibility(View.GONE);
            receiverMessageTime.setVisibility(View.GONE);
            receiverImageGroupMessages.setVisibility(View.GONE);
            receiverGroupPlayTime.setVisibility(View.GONE);
            receiverGroupPlayRecord.setVisibility(View.GONE);
            playVideo2Group.setVisibility(View.GONE);
            frameLayout2Group.setVisibility(View.GONE);
            receiverGroupVideoMessage.setVisibility(View.GONE);
        }
    }
}

