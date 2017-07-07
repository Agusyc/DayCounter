package com.agusyc.daycounter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.ArrayList;

class WidgetListAdapter extends ArrayAdapter<Widget> {
    WidgetListAdapter(Context context, ArrayList<Widget> alarms) {
        super(context, 0, alarms);
    }

    // This method runs as much times as there are alarms defined by the user, so it adds everyone to the listview.
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        // We get the widget for the current position
        final Widget widget = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.widget_row, parent, false);
        }

        assert widget != null;
        long date = widget.getDate();
        long currentTime = System.currentTimeMillis();

        int difference = Days.daysBetween(new DateTime(date), new DateTime(currentTime)).getDays();

        TextView txtDays = convertView.findViewById(R.id.txtDays);

        // We set the days text to the *absolute* difference
        txtDays.setText(Integer.toString(Math.abs(difference)));

        TextView txtLabel = convertView.findViewById(R.id.txtLabel);

        // We check the sign of the number (Positive or negative)
        if (difference > 0) {
            txtLabel.setText(getContext().getString(R.string.days_since) + " " + widget.getLabel());
        } else if (difference < 0) {
            txtLabel.setText(getContext().getString(R.string.days_until) + " " + widget.getLabel());
        } else {
            txtLabel.setText(getContext().getString(R.string.there_are_no_days_since) + " " + widget.getLabel() + ". " + getContext().getString(R.string.today));
            txtLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 24);

            txtDays.setVisibility(View.GONE);
            convertView.findViewById(R.id.txtThereAre).setVisibility(View.GONE);
        }

        Log.d("WidgetUpdater", "Adding widget " + widget.getID() + " with label " + widget.getLabel() + ", original/target date " + date);

        Log.d("WidgetUpdater", "The new difference is " + difference + ". The current time is " + currentTime);

        convertView.setBackgroundColor(widget.getColor());

        // Return the completed view to render on the main activity
        return convertView;
    }
}