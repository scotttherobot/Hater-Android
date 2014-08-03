package com.scotttherobot.hater.app;

import android.app.ActivityManager;
import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.List;

/**
 * Created by scottvanderlind on 7/28/14.
 */
public class GcmIntentService extends IntentService {

    private Handler handler;
    private NotificationManager mNotificationManager;
    public static final int NOTIFICATION_ID = 1;


    public GcmIntentService() {
        super("GcmIntentService");
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
        handler = new Handler();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("GCM", "Handling GCM intent!");
        Bundle extras = intent.getExtras();
        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
        String messageType = gcm.getMessageType(intent);

        if (!extras.isEmpty()) {
            if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE.equals(messageType)) {
                Log.i("GCM", "GCM message received!! " + extras.toString());
                try {
                    String title = extras.getString("title");
                    String message = extras.getString("message");

                    //showToast(title + " " + message);
                    sendNotification(title, message);
                } catch (Exception e) {
                    Log.i("GCM", "Exception trying to get bundle extras");
                }
            }
        }
    }

    private void sendNotification(String title, String msg) {
        mNotificationManager = (NotificationManager)
                this.getSystemService(Context.NOTIFICATION_SERVICE);

        // See if the device is awake. If it's not:
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> services = activityManager
                .getRunningTasks(Integer.MAX_VALUE);

        if (services.get(0).topActivity.getPackageName().toString()
                .equalsIgnoreCase(getApplicationContext().getPackageName().toString())) {
        }

        Intent threadIntent = new Intent(this, EnemiesActivity.class);
        threadIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, threadIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this)
                        .setSmallIcon(R.drawable.brittany_icon)
                        .setContentTitle(title)
                        .setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_SOUND | Notification.FLAG_SHOW_LIGHTS)
                        .setLights(0xFFE40045, 300, 100)
                        .setStyle(new NotificationCompat.BigTextStyle()
                                .bigText(msg))
                        .setContentText(msg);
        //mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setAutoCancel(true);

        mBuilder.setContentIntent(contentIntent);
        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

    // For debugging we can pop a toast every time we get a push.
    public void showToast(String message){
        final String toastmessage = message;
        handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), toastmessage, Toast.LENGTH_LONG).show();
            }
        });

    }
}
