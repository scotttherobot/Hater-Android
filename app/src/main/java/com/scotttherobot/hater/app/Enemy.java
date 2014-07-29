package com.scotttherobot.hater.app;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by scottvanderlind on 7/28/14.
 */
public class Enemy implements Serializable {

    public String username;
    public int id;

    public Enemy(JSONObject object) {
        try {
            this.username = object.getString("username");
            this.id = Integer.parseInt(object.getString("id"));
        } catch (Exception e) {
            Log.e("Enemy", "There was some issue parsing an enemy object");
        }
    }

    public static ArrayList<Enemy> fromJson(JSONArray jsonObjects) {
        ArrayList<Enemy> enemies = new ArrayList<Enemy>();
        for (int i = 0; i < jsonObjects.length(); i++) {
            try {
                enemies.add(new Enemy(jsonObjects.getJSONObject(i)));
            } catch (Exception e) {
                Log.e("Enemy", "There was some issue converting to an arraylist");
            }
        }
        return enemies;
    }


}
