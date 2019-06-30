/*
    In this Activity create video viewer to open the video when we click on it
    the activity receive the URI of the video from the chat or group activity
    and display it on the screen
 */

package com.abuTawfeek.diaaldinkr.friendat;

import android.media.MediaPlayer;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.VideoView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class VideoViewer extends AppCompatActivity {
    private VideoView video;
    private Toolbar mToolbar;
    private DatabaseReference usersRef;
    private ImageButton playVideo, pauseVideo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_viewer);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        video = findViewById(R.id.videoView);
        mToolbar = findViewById(R.id.videoViewer_toolbar);
        playVideo = findViewById(R.id.play_video);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        final String videoURI = getIntent().getExtras().get("video").toString();
        String senderId = getIntent().getExtras().get("sender_id").toString();
        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
               video.setVideoURI(Uri.parse(videoURI));
               video.seekTo( 1 );
                getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        playVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!video.isPlaying()) {
                    video.start();
                    playVideo.setVisibility(View.GONE);
                }

            }
        });
        video.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(video.isPlaying()) {
                    video.pause();
                    playVideo.setVisibility(View.VISIBLE);
                }
            }
        });
        video.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                video.seekTo( 1 );
                playVideo.setVisibility(View.VISIBLE);
            }
        });
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }
}
