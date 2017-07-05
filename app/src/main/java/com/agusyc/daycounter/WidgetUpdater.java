package com.agusyc.daycounter;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.RemoteViews;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class WidgetUpdater extends AppWidgetProvider {

    @Override
    public void onReceive(Context context, Intent intent) {
        // The receiver caught a broadcast:
        Log.d("WidgetUpdater", "Broadcast received! Action: " + intent.getAction());
        // If the action isn't null, we check if the broadcast is telling us that a widget has been deleted
        if (intent.getAction() != null) {
            if (intent.getAction().equals("android.appwidget.action.APPWIDGET_DELETED")) {
                // We call the deleteWidget method, that deletes the widget from the prefs
                deleteWidget(context, intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
            }
        }

        // This means that the broadcast was sent from the UpdateBroadcastReceiver:
        if (intent.hasExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS)) {
            Log.d("WidgetUpdater", "IDs available. Updating widgets...");
            int[] ids = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            // We update the widgets
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        } else {
            // This is in case it wasn't.
            super.onReceive(context, intent);
        }
    }

    private void deleteWidget(Context context, int id) {
        Log.d("MainActivity", "Removing widget with id " + id);

        // We remove the label and date from the preferences
        SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);
        prefs.edit().remove(id + "label").apply();
        prefs.edit().remove(id + "date").apply();

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

            String type;

            // We check the sign of the number (Positive or negative). So we know if we use "since" or "until"
            // TODO: Add special case for when the number is 0
            if (difference > 0) {
                type = context.getString(R.string.days_since);
            } else {
                type = context.getString(R.string.days_until);
            }

            views.setTextViewText(R.id.txtLabel, type + " " + label);

            Log.d("WidgetUpdater", "Updating widget " + appWidgetId + " with label " + label + ", original/target date " + date);

            Log.d("WidgetUpdater", "The new difference is " + difference + ". The current time is " + currentTime);

            // We set the days text to the *absolute* difference
            views.setTextViewText(R.id.txtDays, Integer.toString(Math.abs(difference)));

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width  = Math.round(100 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            int height = Math.round(40 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(prefs.getInt(appWidgetId + "color", Color.BLUE));

            views.setImageViewBitmap(R.id.bkgView, getRoundedCornerStrokedBitmap(context, bmp, 5, 5));

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    private Bitmap getRoundedCornerStrokedBitmap(Context context, Bitmap bitmap, int widthDp, int heightDp) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);


        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        final int heightPx = Math.round(heightDp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
        final int widthPx = Math.round(widthDp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, (float) widthPx, (float) heightPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
}
