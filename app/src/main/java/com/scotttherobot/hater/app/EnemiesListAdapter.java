package com.scotttherobot.hater.app;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by scottvanderlind on 7/28/14.
 */
public class EnemiesListAdapter extends ArrayAdapter<Enemy> {

    public static class ViewHolder {
        TextView username;
    }

    public EnemiesListAdapter(Context context, ArrayList<Enemy> enemies) {
        super(context, R.layout.enemieslist_item, enemies);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Enemy enemy = getItem(position);

        ViewHolder viewHolder;
        if (true || convertView == null) {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.enemieslist_item, null);
            viewHolder.username = (TextView) convertView.findViewById(R.id.enemyName);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.username.setText(enemy.username);

        return convertView;
    }
}
