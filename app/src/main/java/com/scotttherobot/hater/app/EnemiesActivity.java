package com.scotttherobot.hater.app;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;


public class EnemiesActivity extends ActionBarActivity {

    ListView enemiesListView;
    ArrayList<Enemy> enemiesList = new ArrayList<Enemy>();
    public static final String PROPERTY_REG_ID = "registration_id";
    private static final String PROPERTY_APP_VERSION = "appVersion";
    String SENDER_ID = "957490803190";
    private static GoogleCloudMessaging gcm;

    static final int LOGIN_INTENT = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enemies);
        this.enemiesListView = (ListView) findViewById(R.id.enemiesListView);

        // And we need to tell the API where we are so it can do stuff like settings.
        ApiClient.setContext(getApplicationContext());

        // Look to see if we're logged in. If we're not, launch the login modal.


        if (!ApiClient.getCredentials()) {
            // We're logged out.
            showLogin();
        } else {
            ApiClient.loginWithSavedCredentials(new ApiClient.loginHandler() {
                @Override
                public void onLogin(JSONObject response) {
                    registerForPushNotifications();
                    getEnemiesList();
                }

                @Override
                public void onFailure(JSONObject response) {
                    Log.i("Enemies", "There was an error");
                    toast(response.toString());
                }
            });
        }

    }

    protected void showLogin() {
        // We're logged out.
        Intent loginIntent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivityForResult(loginIntent, LOGIN_INTENT);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case LOGIN_INTENT:
                registerForPushNotifications();
                getEnemiesList();
                break;
            default:
                break;
        }
    }

    public void getEnemiesList() {
        ApiClient.get("enemies", null, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                Log.i("Enemies", "Got the roster " + response.toString());
                enemiesList.clear();
                enemiesList.addAll(Enemy.fromJson(response));
                addDataToList();
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable t) {
                Log.i("ENEMIES", "There was an error " + responseString);
                toast(responseString);
            }
        });
    }

    public void addDataToList() {
        EnemiesListAdapter ela = new EnemiesListAdapter(this, enemiesList);
        ListAdapter la = ela;
        enemiesListView.setAdapter(la);
        enemiesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Enemy enemy = enemiesList.get(+position);
                RequestParams p = new RequestParams();
                p.put("target_user", enemy.id);
                p.put("insult_id", 1);
                ApiClient.post("hate", p, new JsonHttpResponseHandler() {
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        toast(response.toString());
                    }
                });
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
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
        switch (item.getItemId()) {
            case R.id.settingsButton:
                Intent settingsIntent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(settingsIntent);
                return true;
            case R.id.refreshButton:
                getEnemiesList();
                return true;
            case R.id.logoutButton:
                ApiClient.clearCredentials();
                ApiClient.logout();
                clearRegistrationId(getApplicationContext());
                toast("Credentials cleared");
                showLogin();
                return true;
            case R.id.addEnemyButton:
                addEnemy();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void addEnemy() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setTitle("Add Enemy");
        final EditText input = new EditText(this);
        input.setHint("username");
        alert.setView(input);
        alert.setPositiveButton("Okay", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                String name = input.getText().toString();
                if (name.trim().isEmpty()) {
                    showAlert("Error", "You must provide a username.");
                    return;
                }
                addEnemyByUsername(name);
            }
        });
        alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alert.show();
    }

    public void addEnemyByUsername(String username) {
        RequestParams rp = new RequestParams();
        rp.add("username", username);
        ApiClient.post("enemies", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                // At this point I don't even care what the response is.
                toast("User added!");
                getEnemiesList();
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable t) {
                toast("Error: " + responseString);
            }
        });
    }

    public void showAlert(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setTitle(title)
                .setNeutralButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
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
        }
    }

    private void handleNewGCMId(String token) {
        // This handles when a *new* gcm token is acquired.
        // This way, we only upload it once.
        Log.v("ENEMIES", "GCM TOKEN is " + token);
        if (!token.isEmpty()) {
            storeRegistrationId(getApplicationContext(), token);
            RequestParams p = new RequestParams();
            p.put("device_type", "ANDROID");
            p.put("token", token);
            ApiClient.post("devices", p, new JsonHttpResponseHandler() {
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    toast(response.toString());
                }
            });

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

    private void clearRegistrationId(Context context) {
        final SharedPreferences prefs = getGCMPreferences(context);
        int appVersion = getAppVersion(context);
        Log.i("ENEMIES", "Clearing regId on app version " + appVersion);
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
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
