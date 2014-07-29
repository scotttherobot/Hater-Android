package com.scotttherobot.hater.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;


public class EnemiesActivity extends ActionBarActivity {

    Button regButton;
    TextView regLabel;
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    String SENDER_ID = "957490803190";
    private static GoogleCloudMessaging gcm;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enemies);

        this.regButton = (Button) findViewById(R.id.regButton);
        this.regLabel = (TextView) findViewById(R.id.regLabel);
    }

    public void registrationClicked(View v) {
        registerForPushNotifications();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.enemies, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void registerForPushNotifications() {
        Log.v("ENEMIES", "Attempting to register for push");

        gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
        // Attempt to get it from sharedprefs
        String regid = getRegistrationId(getApplicationContext());
        // uncomment below to force it empty.
        // regid = "";
        // If it's not in sharedprefs, create one.
        if (regid.isEmpty()) {
            new RegisterBackground().execute();
        } else {
            regLabel.setText("saved: " + regid);
        }
    }

    private void handleNewGCMId(String token) {
        // In the future this will upload the token to the API
        // For now, it logs it and saves it.
        Log.v("ENEMIES", "GCM TOKEN is " + token);
        if (!token.isEmpty()) {
            storeRegistrationId(getApplicationContext(), token);
            this.regLabel.setText(token);
        } else {
            Log.e("ENEMIES", "handleNewGCMId Token Empty!");
        }
    }

    class RegisterBackground extends AsyncTask<String,String,String>{
        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String msg = "";
            try {
                if (gcm == null) {
                    gcm = GoogleCloudMessaging.getInstance(getApplicationContext());
                }
                String regid = gcm.register(SENDER_ID);
                msg = regid;
                Log.i("ENEMIES", "Registered: " + msg);
            } catch (IOException ex) {
                Log.e("ENEMIES", "GCM registration FAILED " + ex.getMessage());
                //msg = "Error :" + ex.getMessage();
                msg = "";
            }
            return msg;
        }
        @Override
        protected void onPostExecute(String msg) {
            Log.v("ENEMIES", msg);
            //sendUuidToBackend(msg);
            handleNewGCMId(msg);
        }
    }

    /**
     * Stores the registration ID and app versionCode in the application's
     * {@code SharedPreferences}.
     *
     * @param context application's context.
     * @param regId registration ID
     */
    private void storeRegistrationId(Context context, String regId) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("ENEMIES", "Saving regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(PROPERTY_REG_ID, regId);
        editor.putInt(PROPERTY_APP_VERSION, appVersion);
        editor.commit();
    }

    /**
     * Gets the current registration ID for application on GCM service.
     * <p>
     * If result is empty, the app needs to register.
     *
     * @return registration ID, or empty string if there is no existing
     *         registration ID.
     */
    private String getRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        String registrationId = prefs.getString(PROPERTY_REG_ID, "");
        if (registrationId.isEmpty()) {
            Log.i("LOGIN", "Registration not found.");
            return "";
        }
        // Check if app was updated; if so, it must clear the registration ID
        // since the existing regID is not guaranteed to work with the new
        // app version.
        int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
        int currentVersion = getAppVersion(context);
        if (registeredVersion != currentVersion) {
            Log.i("ENEMIES", "App version changed.");
            return "";
        }
        return registrationId;
    }

    /**
     * @return Application's {@code SharedPreferences}.
     */
    private SharedPreferences getGCMPreferences(Context context) {
        // This sample app persists the registration ID in shared preferences, but
        // how you store the regID in your app is up to you.
        return getSharedPreferences(EnemiesActivity.class.getSimpleName(),
                Context.MODE_PRIVATE);
    }

    /**
     * @return Application's version code from the {@code PackageManager}.
     */
    private static int getAppVersion(Context context) {
        try {
            PackageInfo packageInfo = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            // should never happen
            throw new RuntimeException("Could not get package name: " + e);
        }
    }

}
