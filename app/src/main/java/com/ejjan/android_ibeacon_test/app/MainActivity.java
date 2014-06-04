package com.ejjan.android_ibeacon_test.app;

import android.graphics.Color;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.radiusnetworks.ibeacon.IBeacon;
import com.radiusnetworks.ibeacon.IBeaconConsumer;
import com.radiusnetworks.ibeacon.IBeaconManager;
import com.radiusnetworks.ibeacon.MonitorNotifier;
import com.radiusnetworks.ibeacon.RangeNotifier;
import com.radiusnetworks.ibeacon.Region;

import java.util.Collection;


public class MainActivity extends ActionBarActivity implements IBeaconConsumer {

    public final String UUID = "8492E75F-4FD6-469D-B132-043FE94921D8";
    private IBeaconManager IBManager = IBeaconManager.getInstanceForApplication(this);
    private Region region = new Region("unique id", UUID, null, null);
    private boolean ranging = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        IBManager.bind(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        IBManager.unBind(this);
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

            }

            @Override
            public void didExitRegion(Region region) {
                Log.i("Info", "No beacon in range");

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
}
