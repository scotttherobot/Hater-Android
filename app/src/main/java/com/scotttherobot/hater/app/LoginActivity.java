package com.scotttherobot.hater.app;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;


public class LoginActivity extends ActionBarActivity {

    EditText usernameField;
    EditText passwordField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        this.usernameField = (EditText) findViewById(R.id.usernameField);
        this.passwordField = (EditText) findViewById(R.id.passwordField);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.login, menu);
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

    public void loginButtonPressed(View v) {
        String username = usernameField.getText().toString();
        String password = passwordField.getText().toString();

        ApiClient.login(username, password, new ApiClient.loginHandler() {
            @Override
            public void onLogin(JSONObject response) {
                toast(response.toString());
                finish();
            }

            @Override
            public void onFailure(JSONObject response) {
                toast(response.toString());
            }
        });
    }

    private void toast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

}
