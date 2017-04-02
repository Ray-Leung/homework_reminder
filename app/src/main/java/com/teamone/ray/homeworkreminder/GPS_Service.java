package com.teamone.ray.homeworkreminder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.places.Place;
import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class GPS_Service extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    public static final String CREATE = "CREATE";
    public static final String RESTART = "DESTROY";
    public static final String LOG_TAG = "Alarm_Service";

    public static final String EXTRA_PARAM1 = "items";
    public static final String EXTRA_PARAM2 = "place";

    private LocationManager manager;
    LocationListener[] locationListeners = new LocationListener[]{
            new LocationListener(LocationManager.GPS_PROVIDER),
            new LocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private static final int NETWORK_LOCATION_INTERVAL = 2000;
    private static final int GPS_LOCATION_INTERVAL = 10000;
    private static final float LOCATION_DISTANCE = 5f;
    private static double cur_longitude;
    private static double cur_latitude;

    public GPS_Service() {
        super("GPS_Service");
    }

    private class LocationListener implements android.location.LocationListener {
        Location cur;

        public LocationListener(String provider) {
            cur = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            cur.set(location);
            cur_latitude = cur.getLatitude();
            cur_longitude = cur.getLongitude();
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }

    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service Started.");
        if (intent != null) {
            final String action = intent.getAction();
            if (CREATE.equals(action)) {
                Bundle b = intent.getExtras();

                final ArrayList<Items> items = b.getParcelableArrayList(EXTRA_PARAM1);
                final LatLng place = b.getParcelable(EXTRA_PARAM2);

                create(items, place);

            } else if (RESTART.equals(action)) {
                Bundle b = intent.getExtras();
                final ArrayList<Items> items = b.getParcelableArrayList(EXTRA_PARAM1);
                restart(items);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private void create(ArrayList<Items> items, LatLng home) {
        TelephonyManager telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNo = telephonyManager.getLine1Number();
        SmsManager sms = SmsManager.getDefault();

        initLocationManger();

        try {
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            manager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, NETWORK_LOCATION_INTERVAL,
                    LOCATION_DISTANCE, locationListeners[1]);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "network provider does not exist, " + e.getMessage());
        } catch (SecurityException e) {
            Log.i(LOG_TAG, "fail to request location update, ignore", e);
        }

        try {
            manager.requestLocationUpdates(LocationManager.GPS_PROVIDER, GPS_LOCATION_INTERVAL,
                    LOCATION_DISTANCE, locationListeners[0]);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "network provider does not exist, " + e.getMessage());
        } catch (SecurityException e) {
            Log.i(LOG_TAG, "fail to request location update, ignore", e);
        }

        while (!items.isEmpty()) {
            int i = 0;
            while (i != items.size()) {
                Items tmp = items.get(i);

                if (compareTime(tmp) > 0) {
                        if (comparePlace(home)) {
                            sms.sendTextMessage(myPhoneNo, null, "Need to finish " + tmp.getTxt() + " today.", null, null);
                            Toast toast = Toast.makeText(this.getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT);
                            toast.show();
                            items.remove(tmp);
                        }
                } else if (compareTime(tmp) < 0) {
                    items.remove(tmp);
                    Log.d(LOG_TAG, "Item deleted");
                } else {
                    i++;
                }

                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                }
            }
        }
    }

    private void initLocationManger() {
        if (manager == null) {
            manager = (LocationManager) this.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private boolean comparePlace(LatLng home) {
        double meter = 0.000001;
        if (cur_latitude <= home.latitude + meter && cur_latitude >= home.latitude - meter) {
            if (cur_longitude <= home.longitude + meter && cur_longitude >= home.longitude - meter) {
                return true;
            }
        }
        return false;
    }

    private int compareTime(Items it) {
        DateFormat df = new SimpleDateFormat("MM dd yyyy HH mm");
        String date = df.format(Calendar.getInstance().getTime());
        String[] strs = date.split(" ");
        int mm = Integer.parseInt(strs[0]);
        int dd = Integer.parseInt(strs[1]);
        int yy = Integer.parseInt(strs[2]);
        int hh = Integer.parseInt(strs[3]);
        int min = Integer.parseInt(strs[4]);

        if (yy == it.getD_y()) {
            if (mm == it.getD_m()) {
                if (dd == it.getD_d()) {
                    if (hh == it.getD_h()) {
                        if (min == it.getD_minu()) {
                            return 0;
                        } else if (min > it.getD_minu()) {
                            return -1;
                        } else {
                            return 1;
                        }
                    } else if (hh > it.getD_h()) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (dd > it.getD_d()) {
                    return -1;
                } else {
                    return 1;
                }
            } else if (mm > it.getD_m()) {
                return -1;
            } else {
                return 1;
            }
        } else if (yy > it.getD_y()) {
            return -1;
        } else {
            return 1;
        }
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void restart(ArrayList<Items> items) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service Created.");
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.getApplicationContext(), "GPS Service created.", duration);
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (manager != null) {
            for (int i = 0; i < locationListeners.length; i++) {
                try {
                    manager.removeUpdates(locationListeners[i]);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        Log.d(LOG_TAG, "Service Destroyed.");
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.getApplicationContext(), "GPS Service destroyed.", duration);
        toast.show();
    }
}