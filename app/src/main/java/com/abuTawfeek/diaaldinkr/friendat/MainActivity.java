package com.abuTawfeek.diaaldinkr.friendat;
/*
In this Activity i retrieve and display all the chats from the database and i create the menu
and the nav bar and also update the user status offline or online
*/
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

public class MainActivity extends AppCompatActivity {
    private Toolbar mToolbar;
    private ViewPager myViewPager;
    private TabLayout myTabLayout;
    private TabsAccessorAdapter myTabsAccessorAdapter;

    private FirebaseAuth mAuth;
    private DatabaseReference rootRef;

    private String currentUserID;
    private NotificationManagerCompat notificationManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth= FirebaseAuth.getInstance();
        rootRef= FirebaseDatabase.getInstance().getReference();

        mToolbar=findViewById(R.id.main_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Friendat");

        myViewPager=findViewById(R.id.main_tabs_pager);
        myTabsAccessorAdapter= new TabsAccessorAdapter(getSupportFragmentManager());
        myViewPager.setAdapter(myTabsAccessorAdapter);

        myTabLayout=findViewById(R.id.main_tabs);
        myTabLayout.setupWithViewPager(myViewPager);
        notificationManager = NotificationManagerCompat.from(this);

        startService(new Intent(this, NotificationService.class));
    }

    @Override
    protected void onStart() {
        super.onStart();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser==null){
            sendUserToLoginActivity();
        }
        else{
            updateUserStatus("online");
            VerifyUserExistence();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
                updateUserStatus("offline");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if(currentUser!=null){
                updateUserStatus("online");
        }
    }

    private void VerifyUserExistence() {
        String currentUserID=mAuth.getCurrentUser().getUid();
        rootRef.child("Users").child(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                //if the user is not a new user and he update his profile
                if(dataSnapshot.child("name").exists()){

                }
                else{
                    //if the user is a new user and send the user to update his profile
                    sendUserToSettingsActivity();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });


    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
        //to prevent the user from going back if he click on back button
        loginIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(loginIntent);
        finish();
    }

    @Override
    //To add the menu button to the main activity
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.options_menu,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //item is used to access the position of an option
        super.onOptionsItemSelected(item);
        if(item.getItemId() == R.id.main_logout_options){
            updateUserStatus("offline");
            mAuth.signOut();
            sendUserToLoginActivity();
        }
        if(item.getItemId() == R.id.main_settings_options){
            sendUserToSettingsActivity();
        }
        if(item.getItemId() == R.id.main_find_friends_options){
            sendUserToFindFriendsActivity();
        }
        if(item.getItemId() == R.id.main_create_group_options){
            requestNewGroup();
        }
        return  true;
    }
    private void requestNewGroup() {
        Intent createGroupIntent = new Intent(MainActivity.this,CreateGroupActivity.class);
        startActivity(createGroupIntent);
    }

    private void sendUserToFindFriendsActivity() {
        Intent findFriendsIntent = new Intent(MainActivity.this,FindFriendsActivity.class);
        startActivity(findFriendsIntent);
    }
    private void sendUserToSettingsActivity() {
        Intent settingsIntent = new Intent(MainActivity.this,SettingsActivity.class);
        startActivity(settingsIntent);
    }

    private void updateUserStatus(String state){
        String saveCurrentTime, saveCurrentDate;
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calendar.getTime());

        HashMap<String,Object> onlineStateMap = new HashMap<>();
        onlineStateMap.put("time",saveCurrentTime);
        onlineStateMap.put("date",saveCurrentDate);
        onlineStateMap.put("state",state);

        currentUserID = mAuth.getCurrentUser().getUid();

        rootRef.child("Users").child(currentUserID).child("user_state").updateChildren(onlineStateMap);



    }
}
