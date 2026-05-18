package com.example.test1;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.naver.maps.geometry.LatLng;

import java.util.ArrayList;
import java.util.List;

public class MapService extends Service {

    private static List<LatLng> recordedCoords = new ArrayList<>();
    private FusedLocationProviderClient locationClient;
    private LocationCallback locationCallback;
    private static LatLng lastKnownLocation;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        locationClient = LocationServices.getFusedLocationProviderClient(this);

        // 위치 콜백 설정
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                // 위치 업데이트
                for (Location location : locationResult.getLocations()) {
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    recordedCoords.add(latLng);
                    lastKnownLocation = latLng; // 마지막 위치 업데이트
                }
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        startForegroundService();
        requestLocationUpdates();
        return START_STICKY;
    }

    private void startForegroundService() {
        String channelId = "map_service_channel";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(
                    channelId,
                    "Map Service",
                    NotificationManager.IMPORTANCE_LOW
            );
        }
        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("Tracking Location")
                .setContentText("Location service is running")
                .setSmallIcon(R.drawable.ic_location)
                .build();

        startForeground(1, notification);
    }

    private void requestLocationUpdates() {
        LocationRequest locationRequest = new LocationRequest.Builder(
                LocationRequest.PRIORITY_HIGH_ACCURACY,
                30000 // 30초 간격으로 업데이트
        )
                .setMinUpdateIntervalMillis(10000)
                .setWaitForAccurateLocation(true)
                .build();


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            return;
        }

        // 위치 업데이트 요청
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 위치 업데이트 중지
        locationClient.removeLocationUpdates(locationCallback);
    }

    public static List<LatLng> getRecordedCoords() {
        return recordedCoords;
    }

    public static LatLng getLastKnownLocation() {
        return lastKnownLocation;
    }
}
