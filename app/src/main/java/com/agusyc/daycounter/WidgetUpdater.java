package com.agusyc.daycounter;

import android.app.PendingIntent;
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
import android.util.TypedValue;
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
        } else if (WIDGET_BUTTON.equals(intent.getAction())) {
            Log.d("WidgetUpdater", "The reset counter button was clicked");

            // Here, the button was clicked
            SharedPreferences prefs = context.getSharedPreferences("DaysPrefs", Context.MODE_PRIVATE);

            String key_base = Integer.toString(intent.getIntExtra("id", 0));
            prefs.edit().putLong(key_base + "date", System.currentTimeMillis()).apply();
            onUpdate(context, AppWidgetManager.getInstance(context), new int[]{intent.getIntExtra("id", 0)});
        } else if (AppWidgetManager.ACTION_APPWIDGET_UPDATE.equals(intent.getAction())) {
            Log.d("WidgetUpdater", "Updating widgets...");
            int[] ids = intent.getExtras().getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
            // We update the widgets
            this.onUpdate(context, AppWidgetManager.getInstance(context), ids);
        }
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
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_since) + " " + label);

                Intent intent = new Intent(context, getClass());
                intent.putExtra("id", appWidgetId);
                intent.setAction(WIDGET_BUTTON);

                views.setOnClickPendingIntent(R.id.btnReset, PendingIntent.getBroadcast(context, 0, intent, 0));
                views.setViewVisibility(R.id.btnReset, View.VISIBLE);
                // We check the sign of the number (Positive or negative)
            } else if (difference < 0) {
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.days_until) + " " + label);
                views.setViewVisibility(R.id.btnReset, View.GONE);
            } else {
                views.setTextViewText(R.id.txtLabel, context.getString(R.string.there_are_no_days_since) + " " + label + ". " + context.getString(R.string.today));
                views.setTextViewTextSize(R.id.txtLabel, TypedValue.COMPLEX_UNIT_SP, 27);

                views.setViewVisibility(R.id.btnReset, View.GONE);
                views.setViewVisibility(R.id.txtDays, View.GONE);
                views.setViewVisibility(R.id.txtThereAre, View.GONE);
            }

            Log.d("WidgetUpdater", "Updating widget " + appWidgetId + " with label " + label + ", original/target date " + date);

            Log.d("WidgetUpdater", "The new difference is " + difference + ". The current time is " + currentTime);

            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int width = Math.round(100 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            int height = Math.round(40 * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));

            Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bmp);
            canvas.drawColor(prefs.getInt(appWidgetId + "color", Color.BLUE));

            views.setImageViewBitmap(R.id.bkgView, getRoundedCornerStrokedBitmap(context, bmp, 3, 3));

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
