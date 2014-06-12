package com.ejjan.android_ibeacon_test.app;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Environment;
import android.os.RemoteException;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;


public class MainActivity extends ActionBarActivity implements IBeaconConsumer {

    private final String UUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private IBeaconManager IBManager = IBeaconManager.getInstanceForApplication(this);
    private Region region = new Region("unique id", UUID, null, null);
    private boolean ranging = false;
    private ArrayList<Long> startTimes;
    private ArrayList<Long> stopTimes;
    private static final String TIME_FILE = "times_file";
    private static final String TIME_SPLITTER= " ";
    private static final String PART_SPLITTER = ";";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startTimes = new ArrayList<Long>();
        stopTimes = new ArrayList<Long>();
        readTimesFromFile();

        IBManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IBManager.unBind(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        writeTimesToFile();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
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

    @Override
    public void onIBeaconServiceConnect() {

        IBManager.setMonitorNotifier(new MonitorNotifier() {
            @Override
            public void didEnterRegion(Region region) {
                Log.i("Info", "A beacon is in range");

                sendNotification("Hello you!");
                saveWorkTime(true);
            }

            @Override
            public void didExitRegion(Region region) {
                Log.i("Info", "No beacon in range");

                sendNotification("Bye, you are now leaving");
                saveWorkTime(false);
            }

            @Override
            public void didDetermineStateForRegion(int i, Region region) {

                if (i == MonitorNotifier.INSIDE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout backgroundLayout = (LinearLayout) findViewById(R.id.background);
                            backgroundLayout.setBackgroundColor(Color.GREEN);
                            ranging = true;
                        }
                    });
                }
                else if (i == MonitorNotifier.OUTSIDE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            LinearLayout backgroundLayout = (LinearLayout) findViewById(R.id.background);
                            backgroundLayout.setBackgroundColor(Color.RED);
                            TextView proximityView = (TextView) findViewById(R.id.proximityLabel);
                            proximityView.setText("Unknown");
                            ranging  = false;
                        }
                    });
                }
            }
        });

        IBManager.setRangeNotifier(new RangeNotifier() {
            @Override
            public void didRangeBeaconsInRegion(Collection<IBeacon> iBeacons, Region region) {
                if (iBeacons.size() > 0 && ranging) {
                    final IBeacon beacon = iBeacons.iterator().next();
                    final String proximity;
                    if(beacon.getProximity() == IBeacon.PROXIMITY_IMMEDIATE)
                        proximity = "Immediate";
                    else if(beacon.getProximity() == IBeacon.PROXIMITY_FAR)
                        proximity = "Far";
                    else if(beacon.getProximity() == IBeacon.PROXIMITY_NEAR)
                        proximity = "Near";
                    else {
                        proximity = "Unknown";
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            TextView proximityView = (TextView) findViewById(R.id.proximityLabel);
                            proximityView.setText(proximity);


                            TextView labelView = (TextView) findViewById(R.id.helloWorldLabel);
                            labelView.setText(String.valueOf(beacon.getRssi()));
                        }
                    });

                    Log.i("rssi", String.valueOf(beacon.getRssi()));
                }
            }
        });

        try {
            IBManager.startMonitoringBeaconsInRegion(region);
            IBManager.startRangingBeaconsInRegion(region);
            ranging = true;
        } catch (RemoteException e) {

        }

    }

    public void sendNotification(String message) {
        NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(this)
                .setContentTitle("iBeacon reminder")
                .setContentText(message)
                .setSmallIcon(R.drawable.ic_launcher);

        Intent resultIntent = new Intent(this, MainActivity.class);


        NotificationManager mNotificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        mNotificationManager.notify(0, notiBuilder.build());
    }

    private void saveWorkTime(boolean start) {

        Date now = new Date();
        if (start)
            startTimes.add(now.getTime());
        else
            stopTimes.add(now.getTime());
        Log.i("Time saved", now.toString());
    }

    public void viewTimesClicked(View view) {

        Intent intent = new Intent(this, TimesActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("startTimes", startTimes);
        bundle.putSerializable("stopTimes", stopTimes);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    private void writeTimesToFile() {
        try {
            FileOutputStream out = openFileOutput(TIME_FILE, MODE_PRIVATE);
            for (long time : startTimes) {
                out.write((time + TIME_SPLITTER).getBytes());
            }
            out.write(PART_SPLITTER.getBytes());
            for (long time : stopTimes) {
                out.write((time + TIME_SPLITTER).getBytes());
            }
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void readTimesFromFile() {
        try {
            File file = new File(getFilesDir() + "/" + TIME_FILE);
            FileInputStream in = openFileInput(TIME_FILE);
            byte[] bytes = new byte[(int)file.length()];
            in.read(bytes);
            String timesString = new String(bytes);
            String[] timesParts = timesString.split(PART_SPLITTER);
            String[] startTimeStrings = timesParts[0].split(TIME_SPLITTER);
            for (String time : startTimeStrings) {
                startTimes.add(Long.parseLong(time));
            }

            if (timesParts.length > 1) {
                String[] stopTimeStrings = timesParts[1].split(TIME_SPLITTER);
                for (String time : stopTimeStrings) {
                    stopTimes.add(Long.parseLong(time));
                }
            }

            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
