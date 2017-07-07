package com.agusyc.daycounter;

import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

public class UpdaterBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Intent updaterIntent = new Intent(context, WidgetUpdater.class);
        updaterIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        Log.d("UpdaterReceiver", "Broadcast received! Updating widgets...");

        SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);


        Set<String> IDs_set = prefs.getStringSet("ids", new HashSet<String>());

        String[] IDs_array_str = IDs_set.toArray(new String[IDs_set.size()]);

        int[] IDs_array = new int[IDs_array_str.length];

        for (int i = 0; i < IDs_array_str.length; i++) {
            IDs_array[i] = Integer.parseInt(IDs_array_str[i]);
            Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i]);
        }

        updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs_array);

        Log.d("UpdateReceiver", "Telling the WidgetUpdater to start");
        context.sendBroadcast(updaterIntent);
    }
}
