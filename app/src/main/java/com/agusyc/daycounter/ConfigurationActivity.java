package com.agusyc.daycounter;

import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;

import com.pes.androidmaterialcolorpickerdialog.ColorPicker;
import com.pes.androidmaterialcolorpickerdialog.ColorPickerCallback;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import fr.ganfra.materialspinner.MaterialSpinner;

public class ConfigurationActivity extends AppCompatActivity {

    private int mAppWidgetId;
    private int selectedColor;
    private int selectedColor_index;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        final GridLayout colorView = (GridLayout) findViewById(R.id.colorView);

        // We instantiate all the Views
        final EditText edtDays = (EditText) findViewById(R.id.edtDays);

        final EditText edtLabel = (EditText) findViewById(R.id.edtLabel);

        final MaterialSpinner spnType = (MaterialSpinner) findViewById(R.id.spnType);

        ArrayList<String> types = new ArrayList<>();
        types.add(" " + getString(R.string.days_since) + " ");
        types.add(" " + getString(R.string.days_until) + " ");

        // Creating adapter for the Spinner
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnType.setAdapter(dataAdapter);

        final Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        final SharedPreferences prefs = getSharedPreferences("DaysPrefs", MODE_PRIVATE);

        Widget widget;

        if (intent.hasExtra("widget_id")) {
            widget = new Widget(getApplicationContext(), Long.parseLong(intent.getStringExtra("widget_id")));

            selectedColor = widget.getColor();
            selectedColor_index = widget.getColorIndex();

            long date = widget.getDate();
            long currentTime = System.currentTimeMillis();

            int difference = Days.daysBetween(new DateTime(date), new DateTime(currentTime)).getDays();

            DecimalFormat formatter = new DecimalFormat("#,###,###");

            edtDays.setText(formatter.format(Math.abs(difference)));
            edtLabel.setText(widget.getLabel());

            if (difference >= 0) {
                spnType.setSelection(1);
            } else {
                spnType.setSelection(2);
            }

            float[] hsv = new float[3];
            Color.colorToHSV(selectedColor, hsv);
            hsv[2] *= 0.6f; // We make the color darker with this

            ImageView selected_view = (ImageView) colorView.getChildAt(selectedColor_index);
            Drawable checked = ContextCompat.getDrawable(getApplicationContext(), R.drawable.checked);
            Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);
            circle.setColorFilter(new
                            PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP));

            selected_view.setImageDrawable(ColorImageView.getOverlay(circle, checked));
        } else {
            ColorImageView colorImageView = (ColorImageView) colorView.getChildAt(4);

            selectedColor = colorImageView.getColor();
            selectedColor_index = 4;

            float[] hsv = new float[3];
            Color.colorToHSV(selectedColor, hsv);
            hsv[2] *= 0.6f; // We make the color darker with this

            Drawable checked = ContextCompat.getDrawable(getApplicationContext(), R.drawable.checked);
            Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);
            circle.setColorFilter(new
                    PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP));

            colorImageView.setImageDrawable(ColorImageView.getOverlay(circle, checked));
        }

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

                String key_base;

                Set<String> currentIDs_set = prefs.getStringSet("ids", new HashSet<String>());

                if (intent.hasExtra("widget_id")) {
                    key_base = intent.getStringExtra("widget_id");
                } else {
                    key_base = Integer.toString(mAppWidgetId);

                    currentIDs_set.add(key_base);
                }


                try {
                    DateTime date = new DateTime(System.currentTimeMillis());

                    String days_s = edtDays.getText().toString();

                    if (days_s.length() == 0) {
                        edtDays.setError(getString(R.string.input_something_error));
                        return;
                    }

                    int days = Integer.parseInt(days_s);

                    int selectedPosition = spnType.getSelectedItemPosition();

                    switch (selectedPosition) {
                        case 0:
                            spnType.setError(getString(R.string.kind_of_event_error));
                            return;
                        case 1:
                            date = date.minusDays(days);
                            break;
                        case 2:
                            date = date.plusDays(days + 1);
                    }

                    String label = edtLabel.getText().toString();

                    if (label.length() == 0) {
                        edtLabel.setError(getString(R.string.input_something_error));
                        return;
                    }

                    prefs.edit().putString(key_base + "label", edtLabel.getText().toString()).apply();
                    prefs.edit().putLong(key_base + "date", date.getMillis()).apply();
                    prefs.edit().putInt(key_base + "color", selectedColor).apply();
                    prefs.edit().putInt(key_base + "color_index", selectedColor_index).apply();
                    prefs.edit().putStringSet("ids", currentIDs_set).apply();

                    Log.d("ConfigurationActivity", "Added new Widget with label" + edtLabel.getText() + ", ID " + key_base + " and date " + date.getMillis());

                    String[] IDs_array_str = currentIDs_set.toArray(new String[currentIDs_set.size()]);

                    int[] IDs_array = new int[IDs_array_str.length];

                    for (int i = 0; i < IDs_array_str.length; i++) {
                        IDs_array[i] = Integer.parseInt(IDs_array_str[i]);
                        Log.d("UpdateReceiver", "Parsed ID: " + IDs_array[i]);
                    }

                    Intent updaterIntent = new Intent(getApplicationContext(), WidgetUpdater.class);
                    updaterIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

                    updaterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, IDs_array);

                    Log.d("UpdateReceiver", "Telling the WidgetUpdater to start");
                    getApplicationContext().sendBroadcast(updaterIntent);

                    setResult(RESULT_OK, resultValue);
                    finish();
                } catch (NumberFormatException e) {
                    edtDays.setError(getString(R.string.number_too_big_error));
                }
            }
        });

        for (int i = 0; i < colorView.getChildCount() - 1; i++) {
            final ColorImageView v = (ColorImageView) colorView.getChildAt(i);
            final int finalI = i;
            v.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ColorImageView in_view;
                    for (int j = 0; j < colorView.getChildCount() - 1; j++) {
                        in_view = (ColorImageView) colorView.getChildAt(j);
                        Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);
                        circle.setColorFilter(new
                                PorterDuffColorFilter(in_view.getColor(), PorterDuff.Mode.SRC_ATOP));

                        in_view.setImageDrawable(circle);
                    }
                    selectedColor = v.getColor();
                    selectedColor_index = finalI;

                    ((ImageView) colorView.getChildAt(colorView.getChildCount() - 1)).setImageDrawable(getDrawable(R.drawable.custom_color));

                    float[] hsv = new float[3];
                    Color.colorToHSV(selectedColor, hsv);

                    Log.d("ConfigurationActivity", "The brightness is " + (1 - hsv[1] + hsv[2]) / 2);

                    hsv[2] *= 0.6f; // We make the color darker with this

                    ColorImageView clicked_view = (ColorImageView) view;
                    Drawable checked = ContextCompat.getDrawable(getApplicationContext(), R.drawable.checked);
                    Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);

                    circle.setColorFilter(new
                            PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP));

                    clicked_view.setImageDrawable(ColorImageView.getOverlay(circle, checked));
                }
            });
        }

        ImageView btnDate = (ImageView) findViewById(R.id.btnDate);
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getFragmentManager(), "datePicker");
            }
        });

        final ImageView btnCustomColor = (ImageView) findViewById(R.id.btnCustomColor);

        btnCustomColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final ColorPicker cp = new ColorPicker(ConfigurationActivity.this, 255, 255, 255);
                    /* Show color picker dialog */
                cp.show();

                /* Set a new Listener called when user click "select" */
                cp.setCallback(new ColorPickerCallback() {
                    @Override
                    public void onColorChosen(@ColorInt int color) {
                        ColorImageView in_view;
                        for (int j = 0; j < colorView.getChildCount() - 1; j++) {
                            in_view = (ColorImageView) colorView.getChildAt(j);
                            Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);
                            circle.setColorFilter(new
                                    PorterDuffColorFilter(in_view.getColor(), PorterDuff.Mode.SRC_ATOP));

                            in_view.setImageDrawable(circle);
                        }

                        selectedColor = color;
                        selectedColor_index = colorView.getChildCount() - 1;

                        float[] hsv = new float[3];
                        Color.colorToHSV(selectedColor, hsv);

                        Log.d("ConfigurationActivity", "The brightness is " + (((1-hsv[1])+hsv[2])/2));

                        hsv[2] *= 0.6f; // We make the color darker with this

                        Drawable checked = ContextCompat.getDrawable(getApplicationContext(), R.drawable.checked);
                        Drawable circle = ContextCompat.getDrawable(getApplicationContext(), R.drawable.circle);
                        circle.setColorFilter(new
                                PorterDuffColorFilter(selectedColor, PorterDuff.Mode.SRC_ATOP));

                        btnCustomColor.setImageDrawable(ColorImageView.getOverlay(circle, checked));

                        cp.dismiss();
                    }
                });
            }
        });
    }
}
