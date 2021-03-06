/*
    In this Activity create image viewer to open the image when we click on it
    the activity receive the URI of the image from the chat or group activity
    and display it on the screen
    using Picasso library
 */


package com.abuTawfeek.diaaldinkr.friendat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ImageView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ImageViewerActivity extends AppCompatActivity {
    private ImageView image;
    private Toolbar mToolbar;
    private DatabaseReference usersRef;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);
        usersRef = FirebaseDatabase.getInstance().getReference().child("Users");
        image = findViewById(R.id.imageViewer);
        mToolbar = findViewById(R.id.imageViewer_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowCustomEnabled(true);
        final String imageURI = getIntent().getExtras().get("image").toString();
        String senderId = getIntent().getExtras().get("sender_id").toString();
        usersRef.child(senderId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Picasso.get().load(imageURI).placeholder(R.drawable.default_image).into(image);
                getSupportActionBar().setTitle(dataSnapshot.child("name").getValue().toString());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

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
