package com.agusyc.daycounter;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.os.Bundle;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;

import org.joda.time.DateTime;
import org.joda.time.Days;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment implements
        DatePickerDialog.OnDateSetListener {

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceSateate) {

        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    public void onDateSet(DatePicker view, int year, int month, int day) {
        Log.d("DatePickerFragment", "User picked year, month, day: " + year + " " + month + 1 + " " + day);

        EditText edtDays = getActivity().findViewById(R.id.edtDays);
        Spinner spnType = getActivity().findViewById(R.id.spnType);

        long currentTime = System.currentTimeMillis();

        // We use the Joda-Time method to calculate the difference
        int difference = Days.daysBetween(new DateTime().withDate(year, month + 1, day), new DateTime(currentTime)).getDays();

        if (difference >= 0) {
            spnType.setSelection(0);
        } else {
            spnType.setSelection(1);
        }


        // We set the days text to the *absolute* difference
        edtDays.setText(Integer.toString(Math.abs(difference)));
    }
}