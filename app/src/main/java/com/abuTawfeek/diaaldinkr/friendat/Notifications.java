package com.abuTawfeek.diaaldinkr.friendat;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.os.Build;

public class Notifications extends Application {
    public static final String CHANNEL_1_ID = "channel1";
    public static final String CHANNEL_2_ID = "channel2";

    @Override
    public void onCreate(){
        super.onCreate();
        // starting the service which will run in the background and detect a message to notify the user.
        startService(new Intent(this, NotificationService.class));
        createNotificationChannels();
    }

    //Build the notification channel
    private void createNotificationChannels(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel channel1= new NotificationChannel(
                    CHANNEL_1_ID,
                    "Messages Channel",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Messages Channel");

            NotificationManager manager = getSystemService((NotificationManager.class));
            manager.createNotificationChannel(channel1);
            NotificationChannel channel2= new NotificationChannel(
                    CHANNEL_2_ID,
                    "Chat Request",
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel1.setDescription("Chat Request");
            manager.createNotificationChannel(channel2);

        }

    }
}
