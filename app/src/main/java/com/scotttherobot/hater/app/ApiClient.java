package com.scotttherobot.hater.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.loopj.android.http.*;

import org.apache.http.Header;
import org.json.JSONObject;

/**
 * Created by Scott Vanderlind on 2/9/14.
 */
public class ApiClient {

    public interface loginHandler {
        public void onLogin(JSONObject response);
        public void onFailure(JSONObject response);
    }
    private static loginHandler loginDone;

    private static final String BASE_URL = "http://soysauce.land:3000/api/";
    private static String SESSION = null;

    public static String userId;
    public static String globalUsername;
    private static String globalPassword;

    public static Context appContext = null;

    private static AsyncHttpClient client = new AsyncHttpClient();

    public static void loginWithSavedCredentials(loginHandler isDone) {
        //getCredentials();
        login(globalUsername, globalPassword, isDone);
    }

    public static void login(String username, String password, loginHandler isDone) {
        loginDone = isDone;
        Log.v("API", "Attempting login.");

        RequestParams p = new RequestParams();
        p.put("username", username);
        p.put("password", password);

        globalUsername = username;
        globalPassword = password;

        post("login", p, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                Log.i("API", "Response: " + response.toString());

                try {
                    if (statusCode == 200) {
                        SESSION = response.getString("api_token");
                        userId = response.getString("id");
                        saveCredentials();
                        loginDone.onLogin(response);
                    } else {
                        loginDone.onFailure(response);
                    }
                } catch (Exception e) {
                    Log.i("API", "Fetching key failed.");
                    loginDone.onFailure(response);
                    SESSION = null;
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable t) {
                Log.i("API", "Login failed", t);
                Log.i("API", responseString);
                JSONObject response = null;
                loginDone.onFailure(response);
            }
        });
    }

    public static void get(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", SESSION);
        client.get(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void post(String url, RequestParams params, JsonHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", SESSION);
        client.post(getAbsoluteUrl(url), params, responseHandler);
    }

    public static void delete(String url, JsonHttpResponseHandler responseHandler) {
        client.addHeader("Authorization", SESSION);
        client.delete(getAbsoluteUrl(url), responseHandler);
    }

    private static String getAbsoluteUrl(String relativeUrl) {
        return BASE_URL + relativeUrl;
    }

    public static void setContext(Context c) {
        appContext = c;
    }

    public static void saveCredentials () {
        SharedPreferences pref = appContext.getSharedPreferences("h8r", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("session", SESSION);
        editor.putString("username", globalUsername);
        editor.putString("password", globalPassword);
        editor.putString("userid", userId);
        editor.commit();
        Log.v("API", "saved saved credentials for " + globalUsername);
    }

    public static void clearCredentials() {
        SharedPreferences pref = appContext.getSharedPreferences("h8r", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public static void logout() {
        globalUsername = null;
        globalPassword = null;
        SESSION = null;
        userId = null;
    }
    public static Boolean getCredentials () {
        Log.v("API", "Getting saved creds");
        SharedPreferences pref = appContext.getSharedPreferences("h8r", Context.MODE_PRIVATE);
        SESSION = pref.getString("session", null);
        globalUsername = pref.getString("username", null);
        globalPassword = pref.getString("password", null);
        userId = pref.getString("userid", null);
        Log.i("API", "found creds: " + globalUsername + " " + globalPassword + " " + SESSION );
        return userId != null && globalPassword != null && globalUsername != null && SESSION != null;
    }

}
