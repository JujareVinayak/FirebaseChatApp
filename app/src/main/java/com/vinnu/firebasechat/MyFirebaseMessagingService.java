package com.vinnu.firebasechat;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class MyFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = MyFirebaseMessagingService.class.getName();
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        String fromUserId = remoteMessage.getData().get("fromUserId");
        String clickAction = remoteMessage.getNotification().getClickAction();
        Log.d(TAG, "onMessageReceived: "+clickAction);
        Intent resultIntent = new Intent(this,ProfileActivity.class);
        resultIntent.putExtra("userId",fromUserId);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,5,resultIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"channelId")
                .setSmallIcon(R.drawable.notification_icon)
                .setContentTitle(remoteMessage.getNotification().getTitle())
                .setContentText(remoteMessage.getNotification().getBody());
        builder.setContentIntent(pendingIntent);
        int notificationId = (int) System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notificationManager.notify(notificationId,builder.build());
        Intent mainIntent = new Intent(MyFirebaseMessagingService.this, MainActivity.class);
        startActivity(mainIntent);
    }
}
