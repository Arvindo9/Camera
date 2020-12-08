package com.example.camera.utils;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.example.camera.R;
import com.google.firebase.storage.UploadTask;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Author       : Arvindo Mondal
 * Created date : 08-12-2020
 */
public class UploadService extends Service {
    public static final String START_UPLOAD = "START_UPLOAD";
    public static final String CANCEL_UPLOAD = "CANCEL_UPLOAD";
    private static final String CHANNEL_ID = "FILE_UPLOAD";
    private static final int NOTIFICATION_ID = 111;
    private static File file;
    public static final String FileUpdates = "FileUpdates";

    public static void startUpload(Context context, File file) {
        UploadService.file = file;
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(START_UPLOAD);
        context.startService(intent);
    }

    public static void cancelUpload(Context context) {
        Intent intent = new Intent(context, UploadService.class);
        intent.setAction(CANCEL_UPLOAD);
        context.startService(intent);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(@NotNull Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (!TextUtils.isEmpty(action)) {
            switch (action) {
                case START_UPLOAD:
                    if (file != null && file.exists()) {
                        startFileUpload(file);
                    }
                    break;
                case CANCEL_UPLOAD:
                    cancelFileUpload();
                    break;
            }
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void cancelFileUpload() {
        file=null;
        hideNotification();
    }

    private void startFileUpload(@NotNull File file) {
        showUploadNotification(file.getName());
        UploadManger.uploadFile(file, new UploadManger.Manager() {
            @Override
            public void success(UploadTask.TaskSnapshot taskSnapshot) {
                cancelFileUpload();
                sendMessageToActivity(true);
            }

            @Override
            public void failure(Exception exception) {
                cancelFileUpload();
                sendMessageToActivity(false);
            }
        });
    }

    private void sendMessageToActivity(boolean status) {
        Intent intent = new Intent(FileUpdates);
        // You can also include some extra data.
        intent.putExtra("Status", status);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void hideNotification() {
        NotificationManagerCompat.from(this).cancel(NOTIFICATION_ID);
        stopForeground(true);
    }

    private void showUploadNotification(String fileName) {
        String channelName = "My Background Service";
        NotificationChannel chan;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);
        }

        String messageText = "";
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setContentTitle("File Upload")
                .setContentText(messageText)
                //.setSmallIcon(R.drawable.ic_checkmark)
                .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher))
                .setAutoCancel(true);

        startForeground(NOTIFICATION_ID, builder.build());
    }
}
