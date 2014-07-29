package com.scotttherobot.hater.app;

import android.app.IntentService;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;

/**
 * Created by scottvanderlind on 7/28/14.
 */
public class GcmIntentService extends IntentService {

    private Handler handler;

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

                    showToast(title + " " + message);
                } catch (Exception e) {
                    Log.i("GCM", "Exception trying to get bundle extras");
                }
            }
        }
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
