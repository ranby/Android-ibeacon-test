package com.ejjan.android_ibeacon_test.app;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;

import java.util.ArrayList;
import java.util.Date;


public class TimesActivity extends ListActivity {

    private static final long TIME_LIMIT_IN_MS = 60000;
    private ArrayList<Long> startTimes;
    private ArrayList<Long> stopTimes;
    private ArrayList<Date> startDates;
    private ArrayList<Date> stopDates;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_times);

        startTimes = (ArrayList<Long>)getIntent().getSerializableExtra("startTimes");
        stopTimes = (ArrayList<Long>)getIntent().getSerializableExtra("stopTimes");

        if (startTimes != null && stopTimes != null) {
            findValidTimes();
            fillList();
        }
    }

    protected void onStart() {
        super.onStart();
        //need to refresh list here?
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void findValidTimes() {
        //Initalize lists if needed
        if (startDates == null || stopDates == null) {
            startDates = new ArrayList<Date>();
            stopDates = new ArrayList<Date>();
        }
        if (startTimes.size() <= 0)
            return;
        //Check if outside or inside region
        boolean inside = false;
        if (startTimes.size() > stopTimes.size())
            inside = true;

        //Adds first enter date because it should always be valid
        Date firstEntry = new Date(startTimes.get(0));
        startDates.add(firstEntry);

        for (int i = 0; i < stopTimes.size(); i++) {
            //
            if (i == stopTimes.size()-1 && !inside) {
                Date lastEntry = new Date(stopTimes.get(i));
                stopDates.add(lastEntry);
                return;
            }

            long leaveInterval = stopTimes.get(i);
            long enterInterval = startTimes.get(i+1);

            if (enterInterval - leaveInterval > TIME_LIMIT_IN_MS) {
                stopDates.add(new Date(leaveInterval));
                startDates.add(new Date(enterInterval));
            }
        }

    }

    private void fillList() {
        ArrayList<String> timeStrings = new ArrayList<String>();
        if (startTimes.size() < 1)
            return;

        for (int i = 0; i < startDates.size(); i++) {

            String startString = android.text.format.DateFormat.format("dd/MM hh:mm", startDates.get(i)).toString();
            String stopString = "";
            if (i < stopDates.size())
                stopString = android.text.format.DateFormat.format("dd/MM hh:mm", stopDates.get(i)).toString();

            timeStrings.add(startString + " - " + stopString);
        }

        ListAdapter adapter = new ArrayAdapter<String>(this, R.layout.time_row, R.id.time_row, timeStrings);
        setListAdapter(adapter);
    }
}
