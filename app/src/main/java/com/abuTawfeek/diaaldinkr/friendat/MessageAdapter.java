package com.abuTawfeek.diaaldinkr.friendat;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Messages> userMessagesList;
    private FirebaseAuth mAuth;
    private DatabaseReference userRef;
    private Context context;
    private boolean isPlaying = false;
    private int position = 0;
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
        final String messageSenderID = mAuth.getCurrentUser().getUid();
        final Messages messages = userMessagesList.get(i);
        final String fromUserID = messages.getFrom();
        String fromMessageType = messages.getType();
        String messageTime = messages.getTime();

        userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.child(fromUserID).addListenerForSingleValueEvent(new ValueEventListener() {
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
            messageViewHolder.receiverImageMessages.setVisibility(View.GONE);
            messageViewHolder.receiverPlayRecord.setVisibility(View.GONE);
            messageViewHolder.receiverTimeRecord.setVisibility(View.GONE);

            messageViewHolder.senderImageMessages.setVisibility(View.GONE);
            messageViewHolder.senderImageMessages.setVisibility(View.GONE);
            messageViewHolder.senderPlayRecord.setVisibility(View.GONE);
            messageViewHolder.senderTimeRecord.setVisibility(View.GONE);

            String target_language=messages.getTo();
            //Default variables for translation
            String decrypted = "";
            try {
                decrypted = AESUtils.decrypt(messages.getMessage());
            } catch (Exception e) {
                e.printStackTrace();
            }
            String textToBeTranslated = decrypted;
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
                messageViewHolder.senderMessageText.setText(decrypted);
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
                /**********************>  change the to translate        */
                messageViewHolder.receiverMessageText.setText(messages.getMessage());
                messageViewHolder.receiverMessageText.setText(TranslatedText);
                messageViewHolder.receiverTranslatedMessageText.setText(decrypted);
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
            }
        }else if(fromMessageType.equals("image")){
            messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverPlayRecord.setVisibility(View.GONE);
            messageViewHolder.receiverTimeRecord.setVisibility(View.GONE);
            messageViewHolder.senderMessageText.setVisibility(View.GONE);
            messageViewHolder.senderTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.senderPlayRecord.setVisibility(View.GONE);
            messageViewHolder.senderTimeRecord.setVisibility(View.GONE);
            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverImageMessages.setVisibility(View.GONE);
                messageViewHolder.receiverMessageTime.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);

                messageViewHolder.senderImageMessages.setVisibility(View.VISIBLE);
                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);

                messageViewHolder.senderImageMessages.setBackgroundResource(R.drawable.sender_messages_layout);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.profile_image).into(messageViewHolder.senderImageMessages);
                messageViewHolder.senderMessageTime.setText(messages.getTime());
                messageViewHolder.senderImageMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageViewerIntent = new Intent(context.getApplicationContext(),ImageViewerActivity.class);
                        imageViewerIntent.putExtra("image",messages.getMessage());
                        imageViewerIntent.putExtra("sender_id",fromUserID);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(imageViewerIntent);
                    }
                });
            } else{
                messageViewHolder.senderImageMessages.setVisibility(View.GONE);
                messageViewHolder.senderMessageTime.setVisibility(View.GONE);

                messageViewHolder.receiverImageMessages.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverImageMessages.setBackgroundResource(R.drawable.receiver_messages_layout);
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverImageMessages);
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
                messageViewHolder.receiverImageMessages.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent imageViewerIntent = new Intent(context.getApplicationContext(),ImageViewerActivity.class);
                        imageViewerIntent.putExtra("image",messages.getMessage());
                        imageViewerIntent.putExtra("sender_id",fromUserID);
                        imageViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(imageViewerIntent);
                    }
                });
            }
        }else if(fromMessageType.equals("audio")){
            final MediaPlayer mediaPlayer = new MediaPlayer();
            Uri myUri = Uri.parse(messages.getMessage()); // initialize Uri here
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            try {
                mediaPlayer.setDataSource(context, myUri);
                mediaPlayer.prepare();
            } catch (IOException e) {
                e.printStackTrace();
            }
            messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.senderMessageText.setVisibility(View.GONE);
            messageViewHolder.senderTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverImageMessages.setVisibility(View.GONE);
            messageViewHolder.senderImageMessages.setVisibility(View.GONE);
            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverPlayRecord.setVisibility(View.GONE);
                messageViewHolder.receiverTimeRecord.setVisibility(View.GONE);
                messageViewHolder.receiverTimeRecord.setVisibility(View.GONE);
                messageViewHolder.receiverMessageTime.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);

                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderPlayRecord.setVisibility(View.VISIBLE);
                messageViewHolder.senderTimeRecord.setVisibility(View.VISIBLE);

                messageViewHolder.senderPlayRecord.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageTime.setText(messages.getTime());
                messageViewHolder.senderTimeRecord.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                messageViewHolder.senderPlayRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPlaying){
                            messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                        }else{
                            messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_pause_record);
                            mediaPlayer.start();
                        }
                        isPlaying =!isPlaying;

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.seekTo(0);
                        messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_play);
                        isPlaying =!isPlaying;
                    }
                });
            }else{
                messageViewHolder.senderMessageTime.setVisibility(View.GONE);
                messageViewHolder.senderPlayRecord.setVisibility(View.GONE);
                messageViewHolder.senderTimeRecord.setVisibility(View.GONE);

                messageViewHolder.receiverPlayRecord.setVisibility(View.VISIBLE);
                messageViewHolder.receiverTimeRecord.setVisibility(View.VISIBLE);
                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);

                messageViewHolder.receiverPlayRecord.setBackgroundResource(R.drawable.receiver_messages_layout);
                messageViewHolder.receiverMessageTime.setText(messages.getTime());
                messageViewHolder.receiverTimeRecord.setText(milliSecondsToTimer(mediaPlayer.getDuration()));
                messageViewHolder.receiverPlayRecord.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isPlaying){
                            messageViewHolder.receiverPlayRecord.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                        }else{
                            messageViewHolder.receiverPlayRecord.setImageResource(R.drawable.ic_pause_record);
                            mediaPlayer.start();
                        }
                        isPlaying =!isPlaying;

                    }
                });
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        mp.seekTo(0);
                        messageViewHolder.receiverPlayRecord.setImageResource(R.drawable.ic_play);
                        isPlaying =!isPlaying;
                    }
                });
            }
                            /*
                 Uri myUri = Uri.parse(messages.getMessage()); // initialize Uri here
                        mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
                        try {
                            mediaPlayer.setDataSource(context, myUri);
                            mediaPlayer.prepareAsync();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (isPlaying){
                            messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_play);
                            mediaPlayer.pause();
                            if(position==mediaPlayer.getDuration()){
                                position=0;
                                messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_play);
                            }
                            position=mediaPlayer.getCurrentPosition();
                            mediaPlayer.release();
                        }else{
                            messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_pause_record);
                            if(position==mediaPlayer.getDuration()){
                                position=0;
                                messageViewHolder.senderPlayRecord.setImageResource(R.drawable.ic_play);
                            }
                            mediaPlayer.seekTo(position);
                            mediaPlayer.start();
                        }
                        isPlaying =!isPlaying;

                */
        }else if(fromMessageType.equals("video")){

            messageViewHolder.receiverMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverVideoMessage.setVisibility(View.GONE);
            messageViewHolder.receiverImageMessages.setVisibility(View.GONE);
            messageViewHolder.receiverTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.receiverPlayRecord.setVisibility(View.GONE);
            messageViewHolder.receiverTimeRecord.setVisibility(View.GONE);

            messageViewHolder.senderMessageText.setVisibility(View.GONE);
            messageViewHolder.senderTranslatedMessageText.setVisibility(View.GONE);
            messageViewHolder.senderPlayRecord.setVisibility(View.GONE);
            messageViewHolder.senderTimeRecord.setVisibility(View.GONE);
            messageViewHolder.senderVideoMessage.setVisibility(View.GONE);
            messageViewHolder.senderImageMessages.setVisibility(View.GONE);

            if(fromUserID.equals(messageSenderID)){
                messageViewHolder.receiverMessageTime.setVisibility(View.GONE);
                messageViewHolder.receiverVideoMessage.setVisibility(View.GONE);
                messageViewHolder.receiverProfileImage.setVisibility(View.GONE);
                messageViewHolder.playVideo.setVisibility(View.GONE);
                messageViewHolder.frameLayout.setVisibility(View.GONE);

                messageViewHolder.senderMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.senderVideoMessage.setVisibility(View.VISIBLE);
                messageViewHolder.playVideo2.setVisibility(View.VISIBLE);
                messageViewHolder.frameLayout2.setVisibility(View.VISIBLE);

                //messageViewHolder.senderVideoMessage.setBackgroundResource(R.drawable.sender_messages_layout);
                messageViewHolder.senderMessageTime.setText(messages.getTime());
                Uri myUri = Uri.parse(messages.getMessage()); // initialize Uri here
                messageViewHolder.senderVideoMessage.setVideoURI(myUri);
                messageViewHolder.senderVideoMessage.seekTo( 1 );
                final Intent videoViewerIntent = new Intent(context.getApplicationContext(),VideoViewer.class);
                videoViewerIntent.putExtra("video",messages.getMessage());
                videoViewerIntent.putExtra("sender_id",fromUserID);
                videoViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                messageViewHolder.senderVideoMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
                messageViewHolder.playVideo2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
            } else{
                messageViewHolder.senderMessageTime.setVisibility(View.GONE);
                messageViewHolder.senderVideoMessage.setVisibility(View.GONE);
                messageViewHolder.playVideo2.setVisibility(View.GONE);
                messageViewHolder.frameLayout2.setVisibility(View.GONE);

                messageViewHolder.receiverMessageTime.setVisibility(View.VISIBLE);
                messageViewHolder.receiverVideoMessage.setVisibility(View.VISIBLE);
                messageViewHolder.receiverProfileImage.setVisibility(View.VISIBLE);
                messageViewHolder.playVideo.setVisibility(View.VISIBLE);
                messageViewHolder.frameLayout.setVisibility(View.VISIBLE);


                messageViewHolder.receiverMessageTime.setText(messages.getTime());
                Picasso.get().load(messages.getMessage()).placeholder(R.drawable.profile_image).into(messageViewHolder.receiverImageMessages);
                Uri myUri = Uri.parse(messages.getMessage()); // initialize Uri here
                messageViewHolder.receiverVideoMessage.setVideoURI(myUri);
                messageViewHolder.receiverVideoMessage.seekTo( 1 );
                final Intent videoViewerIntent = new Intent(context.getApplicationContext(),VideoViewer.class);
                videoViewerIntent.putExtra("video",messages.getMessage());
                videoViewerIntent.putExtra("sender_id",fromUserID);
                videoViewerIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                messageViewHolder.receiverVideoMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        context.startActivity(videoViewerIntent);
                    }
                });
                messageViewHolder.playVideo.setOnClickListener(new View.OnClickListener() {
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
        //Function for calling executing the Detector Background Task
    private String Detect(String textToBeDetected){
        DetectorBackgroundTask detectorBackgroundTask= new DetectorBackgroundTask(context);
        String detectionResult = null; // Returns the detection language code as a String
        try {
            detectionResult = detectorBackgroundTask.execute(textToBeDetected).get();
            try {
                final JSONObject translationResultObj = new JSONObject(detectionResult);
                detectionResult=translationResultObj.get("text").toString();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {
            detectionResult = "en";
        }
        return detectionResult;
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
        } catch (NullPointerException e) {
            translationResult = textToBeTranslated;
        }
        return translationResult;
    }
    @Override
    public int getItemCount() {
        return userMessagesList.size();
    }

    public class  MessageViewHolder extends RecyclerView.ViewHolder{
        public TextView senderMessageText, receiverMessageText ,receiverMessageTime, senderMessageTime ,senderTranslatedMessageText
                , receiverTranslatedMessageText, senderTimeRecord, receiverTimeRecord;
        public ImageButton receiverImageMessages, senderImageMessages, playVideo, playVideo2;
        public ImageView senderPlayRecord, receiverPlayRecord;
        public VideoView senderVideoMessage, receiverVideoMessage;
        public CircleImageView receiverProfileImage;
        public FrameLayout frameLayout, frameLayout2;
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageText = itemView.findViewById(R.id.sender_message_text);
            senderTranslatedMessageText = itemView.findViewById(R.id.sender_translated_message_text);
            senderMessageTime = itemView.findViewById(R.id.sender_message_time);
            senderImageMessages = itemView.findViewById(R.id.sender_image_messages);
            senderPlayRecord = itemView.findViewById(R.id.sender_play_record);
            senderTimeRecord = itemView.findViewById(R.id.sender_record_time);
            senderVideoMessage = itemView.findViewById(R.id.sender_video_layout);
            playVideo = itemView.findViewById(R.id.play_video);
            frameLayout = itemView.findViewById(R.id.video_frame);


            receiverMessageText = itemView.findViewById(R.id.receiver_message_text);
            receiverTranslatedMessageText = itemView.findViewById(R.id.receiver_translated_message_text);
            receiverMessageTime = itemView.findViewById(R.id.receiver_message_time);
            receiverProfileImage = itemView.findViewById(R.id.message_profile_image);
            receiverImageMessages = itemView.findViewById(R.id.receiver_image_messages);
            receiverPlayRecord = itemView.findViewById(R.id.receiver_play_record);
            receiverTimeRecord = itemView.findViewById(R.id.receiver_record_time);
            receiverVideoMessage = itemView.findViewById(R.id.receiver_video_layout);
            playVideo2 = itemView.findViewById(R.id.play_video2);
            frameLayout2 = itemView.findViewById(R.id.video_frame2);

            senderMessageText.setVisibility(View.GONE);
            senderTranslatedMessageText.setVisibility(View.GONE);
            senderImageMessages.setVisibility(View.GONE);
            senderPlayRecord.setVisibility(View.GONE);
            senderTimeRecord.setVisibility(View.GONE);
            senderVideoMessage.setVisibility(View.GONE);
            playVideo.setVisibility(View.GONE);
            frameLayout.setVisibility(View.GONE);

            receiverMessageText.setVisibility(View.GONE);
            receiverProfileImage.setVisibility(View.GONE);
            receiverTranslatedMessageText.setVisibility(View.GONE);
            receiverImageMessages.setVisibility(View.GONE);
            receiverPlayRecord.setVisibility(View.GONE);
            receiverTimeRecord.setVisibility(View.GONE);
            receiverVideoMessage.setVisibility(View.GONE);
            playVideo2.setVisibility(View.GONE);
            frameLayout2.setVisibility(View.GONE);

        }
    }
}

