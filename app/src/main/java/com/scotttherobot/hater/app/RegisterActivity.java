package com.scotttherobot.hater.app;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;
import org.json.JSONArray;
import org.json.JSONObject;


public class RegisterActivity extends ActionBarActivity {

    EditText username;
    EditText password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        this.username = (EditText) findViewById(R.id.usernameField);
        this.password = (EditText) findViewById(R.id.passwordField);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.register, menu);
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

    public void registerClicked(View v) {
        submitRegistration();
    }

    public void submitRegistration() {
        final String password = this.password.getText().toString();
        RequestParams rp = new RequestParams();
        rp.add("username", this.username.getText().toString());
        rp.add("password", password);
        ApiClient.post("users", rp, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    String username = response.getString("username");
                    toast("Register success!");
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("username", username);
                    returnIntent.putExtra("password", password);
                    setResult(RESULT_OK, returnIntent);
                    finish();
                } catch (Exception e) {
                    toast("Error. " + e.getMessage());
                }
            }
            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable t) {
                toast("Error: " + responseString);
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
