package com.smartbite.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.smartbite.utils.Constants;

import java.util.HashMap;
import java.util.Map;

public class LocationService extends Service {

    private static final String TAG        = "LocationService";
    private static final String CHANNEL_ID = "LocationChannel";
    private static final int    NOTIF_ID   = 1001;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback            locationCallback;
    private FirebaseFirestore           db;
    private String                      userId;

    @Override
    public void onCreate() {
        super.onCreate();
        db = FirebaseFirestore.getInstance();

        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        createChannel();
        startForeground(NOTIF_ID, buildNotification());
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        LocationRequest request = new LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 5000)
                .setMinUpdateIntervalMillis(3000)
                .build();

        locationCallback = new LocationCallback() {
            // FIX 1: @NonNull annotation to match LocationCallback signature
            @Override
            public void onLocationResult(@NonNull LocationResult result) {
                if (userId == null) return;
                Location location = result.getLastLocation();
                if (location != null) {
                    updateFirestore(location.getLatitude(), location.getLongitude());
                }
            }
        };

        try {
            fusedLocationClient.requestLocationUpdates(
                    request, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            // FIX 2: Log.e instead of printStackTrace() for robust logging
            Log.e(TAG, "Location permission not granted", e);
        }
    }

    private void updateFirestore(double lat, double lng) {
        if (userId == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("currentLat",  lat);
        data.put("currentLng",  lng);
        data.put("lastUpdated", System.currentTimeMillis());

        db.collection(Constants.COLLECTION_AGENTS).document(userId)
                .update(data)
                .addOnFailureListener(e ->
                        db.collection(Constants.COLLECTION_AGENTS)
                                .document(userId)
                                .set(data));
    }

    private void createChannel() {
        // FIX 3: wrap API 26+ NotificationChannel calls in Build.VERSION check
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Location Tracking",
                    NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("SmartBite Delivery")
                .setContentText("Tracking location for delivery")
                // FIX 4: use built-in drawable — mipmap/ic_launcher is not valid for notifications
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationCallback != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}