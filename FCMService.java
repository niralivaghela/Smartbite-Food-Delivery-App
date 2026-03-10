package com.smartbite.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.smartbite.R;
import com.smartbite.activities.HomeActivity;
import com.smartbite.utils.Constants;

public class FCMService extends FirebaseMessagingService {

    private static final String CHANNEL_ID = "SmartBiteNotifications";

    // FIX 1: @NonNull annotation to match FirebaseMessagingService signature
    @Override
    public void onMessageReceived(@NonNull RemoteMessage msg) {
        super.onMessageReceived(msg);
        String title = msg.getNotification() != null ? msg.getNotification().getTitle() : "SmartBite";
        String body  = msg.getNotification() != null ? msg.getNotification().getBody()  : "";
        showNotification(title, body);
    }

    private void showNotification(String title, String body) {
        createChannel();

        Intent intent = new Intent(this, HomeActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE);

        // FIX 2: use @drawable instead of @mipmap — ic_launcher is not in mipmap
        // Using the built-in Android notification icon as a safe fallback.
        // Replace R.drawable.ic_notification with your own icon if you have one.
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setContentIntent(pendingIntent)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body));

        NotificationManager nm =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm != null) nm.notify((int) System.currentTimeMillis(), builder.build());
    }

    private void createChannel() {
        // FIX 3: wrap API 26+ calls in Build.VERSION check to satisfy minSdk 24
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Order Notifications",
                    NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("SmartBite order notifications");
            channel.enableVibration(true);

            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    // FIX 4: @NonNull annotation to match FirebaseMessagingService signature
    @Override
    public void onNewToken(@NonNull String token) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(currentUser.getUid())
                .update("fcmToken", token);
    }
}