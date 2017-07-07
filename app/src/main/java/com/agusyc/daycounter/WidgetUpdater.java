package com.agusyc.daycounter;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WidgetUpdater extends AppWidgetProvider {

    public static String WIDGET_BUTTON = "com.agusyc.daycounter.RESET_COUNTER";

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        // The receiver caught a broadcast:
        Log.d("WidgetUpdater", "Broadcast received!");


        Log.d("WidgetUpdater", "The action is: " + intent.getAction());
        // We check if the broadcast is telling us that a widget has been deleted
        if ("android.appwidget.action.APPWIDGET_DELETED".equals(intent.getAction())) {
            // We call the deleteWidget method, that deletes the widget from the prefs
            deleteWidget(context, intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
        } else if (intent.getAction() != null && intent.getAction().startsWith(WIDGET_BUTTON)) {
            String id_s = intent.getAction().replace(WIDGET_BUTTON, "");

            Log.d("WidgetUpdater", "The reset counter button was clicked for id " + id_s);

            // Here, the button was clicked
            SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);

            prefs.edit().putLong(id_s + "date", System.currentTimeMillis()).apply();
            onUpdate(context, AppWidgetManager.getInstance(context), new int[]{Integer.parseInt(id_s)});
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            Log.d("WidgetUpdater", "Updating widgets...");
            int[] ids = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            // We update the widgets
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        }
    }

    protected PendingIntent getPendingSelfIntent(Context context, String action, int id) {
        Intent intent = new Intent(context, getClass());
        intent.setAction(action + id);
        return PendingIntent.getBroadcast(context, 0, intent, 0);
    }

    private void deleteWidget(Context context, int id) {
        Log.d("MainActivity", "Removing widget with id " + id);

        // We remove the label and date from the preferences
        SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove(id + "label").apply();
        prefs.edit().remove(id + "date").apply();
        prefs.edit().remove(id + "color").apply();
        prefs.edit().remove(id + "color_index").apply();

        Set<String> ids_set = prefs.getStringSet("ids", new HashSet<String>());

        // We iterate trough the whole set, when we find the widget that is being deleted, we delete it from the set without mercy!
        Iterator iterator = ids_set.iterator();
        while (iterator.hasNext()) {
            if (iterator.next().equals(Integer.toString(id))) {
                Log.d("WidgetUpdater", "Removing widget ID from preferences");
                iterator.remove();
                break;
            }
        }

        // We put the new set to the prefs
        prefs.edit().putStringSet("ids", ids_set).apply();
    }

    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Log.d("WidgetUpdater", "WidgetUpdater onUpdate started");

        SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int appWidgetId : appWidgetIds) {

            // We get all the views that are in the widget
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.daycounter);

            // We get all the needed data
            String key_base = Integer.toString(appWidgetId);
            String label = prefs.getString(key_base + "label", "");
            long date = prefs.getLong(key_base + "date", 0);
            long currentTime = System.currentTimeMillis();

            // We use the Joda-Time method to calculate the difference
            int difference = Days.daysBetween(new DateTime(date), new DateTime(currentTime)).getDays();

            // We set the days text to the *absolute* difference
            views.setTextViewText(R.id.txtDays, Integer.toString(Math.abs(difference)));

            // We check the sign of the number (Positive or negative). So we know if we use "since" or "until"
            if (difference > 0) {
                views.setTextViewText(R.id.txtThereAreHaveBeen, context.getString(R.string.there_have_been));
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_since) + " " + label);

                views.setOnClickPendingIntent(R.id.btnReset, getPendingSelfIntent(context, WIDGET_BUTTON, appWidgetId));
                views.setViewVisibility(R.id.btnReset, View.VISIBLE);
                // We check the sign of the number (Positive or negative)
            } else if (difference < 0) {
                views.setTextViewText(R.id.txtThereAreHaveBeen, context.getString(R.string.there_are));
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_until) + " " + label);
                views.setViewVisibility(R.id.btnReset, View.GONE);
                views.setViewVisibility(R.id.divider, View.GONE);
            } else {
                views.setTextViewText(R.id.txtNoDays, context.getString(R.string.there_are_no_days_since) + " " + label + ". " + context.getString(R.string.today));

                views.setViewVisibility(R.id.txtNoDays, View.VISIBLE);
                views.setViewVisibility(R.id.btnReset, View.GONE);
                views.setViewVisibility(R.id.txtDays, View.GONE);
                views.setViewVisibility(R.id.txtThereAreHaveBeen, View.GONE);
                views.setViewVisibility(R.id.txtLabel, View.GONE);
                views.setViewVisibility(R.id.divider, View.GONE);
            }

            Log.d("WidgetUpdater", "Updating widget " + appWidgetId + " with label " + label + ", original/target date " + date);

            Log.d("WidgetUpdater", "The new difference is " + difference + ". The current time is " + currentTime);

            int color = prefs.getInt(appWidgetId + "color", Color.BLUE);

            views.setInt(R.id.bkgView, "setColorFilter", color);

            float[] hsv = new float[3];

            Color.colorToHSV(color, hsv);

            float brightness = (1 - hsv[1] + hsv[2]) / 2;

            if (brightness >= 0.7) {
                views.setTextColor(R.id.txtLabel, Color.BLACK);
                views.setTextColor(R.id.txtDays, Color.BLACK);
                views.setTextColor(R.id.txtThereAreHaveBeen, Color.BLACK);
                views.setTextColor(R.id.txtNoDays, Color.BLACK);
            }

            views.setInt(R.id.lytWidget, "setVisibility", View.VISIBLE);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }
}
