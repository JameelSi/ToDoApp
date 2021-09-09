package com.gameelsi_majdj.ex3;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;


public class AlarmReceiver extends BroadcastReceiver {
    private String user,title;
    private int notifID=1;
    private static final String CHANNEL_ID = "channel_main";
    private static final CharSequence CHANNEL_NAME = "Main Channel";

    @Override
    public void onReceive(Context context, Intent intent) {
        user=intent.getStringExtra("user");
        title=intent.getStringExtra("title");
        // show notifcation
        pushNotification(context);
    }

    public void pushNotification(Context context) {
        // create new notification
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_bell)
                .setContentTitle("Reminder for user: ("+user+")")
                .setContentText(title)
                .build();
        // reference for Notification Manager system Service
        NotificationManager notifMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        // create channel only if api is > 26
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            notifMgr.createNotificationChannel(channel);
        }
        notifMgr.notify(notifID++, notification);
    }


}
