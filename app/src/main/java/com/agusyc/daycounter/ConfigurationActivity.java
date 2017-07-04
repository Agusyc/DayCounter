package com.agusyc.daycounter;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class ConfigurationActivity extends AppCompatActivity {

    private int mAppWidgetId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configuration);

        final EditText edtDays = (EditText) findViewById(R.id.edtDays);

        final EditText edtLabel = (EditText) findViewById(R.id.edtLabel);

        final Spinner spnType = (Spinner) findViewById(R.id.spnType);

        final ConstraintLayout constraintLayout = (ConstraintLayout) findViewById(R.id.lytMain);

        final ConstraintSet set = new ConstraintSet();

        set.clone(constraintLayout);

        ArrayList<String> types = new ArrayList<>();
        types.add(" " + getString(R.string.days_since) + " ");
        types.add(" " + getString(R.string.days_for) + " ");

        // Creating adapter for the Spinner
        final ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, types);

        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spnType.setAdapter(dataAdapter);

        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    edtLabel.setHint(getString(R.string.days_since_hint));
                } else {
                    edtLabel.setHint(getString(R.string.days_for_hint));
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {}
        });

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        Button okButton = (Button) findViewById(R.id.okButton);
        okButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (edtLabel.getText().length() != 0 && edtDays.getText().length() != 0) {
                    Intent resultValue = new Intent();
                    resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

                    SharedPreferences prefs = getSharedPreferences("DaysPrefs", MODE_PRIVATE);

                    String key_base = Integer.toString(mAppWidgetId);

                    Set<String> currentIDs_set = prefs.getStringSet("ids", new HashSet<String>());

                    currentIDs_set.add(key_base);

                    Long date;

                    if (spnType.getSelectedItemPosition() == 0) {
                        date = System.currentTimeMillis() - 86400000 * Integer.parseInt(edtDays.getText().toString());
                    } else {
                        date = System.currentTimeMillis() + 86400000 * Integer.parseInt(edtDays.getText().toString());
                    }

                    prefs.edit().putString(key_base + "label", edtLabel.getText().toString()).apply();
                    prefs.edit().putLong(key_base + "date", date).apply();
                    prefs.edit().putStringSet("ids", currentIDs_set).apply();

                    Log.d("ConfigurationActivity", "Added new Widget with label" + edtLabel.getText() + ", ID " + key_base + " and date " + date);

                    Intent update_intent = new Intent("com.agusyc.daycounter.UPDATE_WIDGETS");
                    sendBroadcast(update_intent);

                    setResult(RESULT_OK, resultValue);
                    finish();
                }
            }
        });
    }
}
