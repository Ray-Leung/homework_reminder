package com.teamone.ray.homeworkreminder;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.v4.app.ActivityCompat;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.wearable.DataApi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions and extra parameters.
 */
public class Alarm_Service extends IntentService {

    protected GoogleApiClient mGoogleApiClient;
    public static final String CREATE = "CREATE";
    public static final String RESTART = "DESTROY";
    public static final String LOG_TAG = "Alarm_Service";

    public static final String EXTRA_PARAM1 = "items";
    public static final String EXTRA_PARAM2 = "place";
    private static long timer = 0;
    private static final long REFRESH_TIME = 1800000;

    private LocationManager manager;
    mLocationListener[] locationListeners = new mLocationListener[]{
            new mLocationListener(LocationManager.GPS_PROVIDER),
            new mLocationListener(LocationManager.NETWORK_PROVIDER)
    };

    private Location loc;
    private static final int NETWORK_LOCATION_INTERVAL = 2000;
    private static final int GPS_LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static double cur_longitude;
    private static double cur_latitude;

    public Alarm_Service() {
        super("Alarm_Service");
    }

    private class mLocationListener implements android.location.LocationListener {
        Location cur;

        public mLocationListener(String provider) {
            cur = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            cur = location;
            cur_latitude = cur.getLatitude();
            cur_longitude = cur.getLongitude();
            Log.d("cur Location change to", cur.toString());
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

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service Started.");
        if (intent != null) {
            final String action = intent.getAction();
            if (CREATE.equals(action)) {
                Bundle b = intent.getExtras();
                final LatLng place = b.getParcelable(EXTRA_PARAM2);

                //final ArrayList<Items> items = b.getParcelableArrayList(EXTRA_PARAM1);
                create(Data.db, place);

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
       /* Collections.sort(items, new Comparator<Items>() {
            @Override
            public int compare(Items it1, Items it2) {
                return it1.getStart_date().compareTo(it2.getStart_date());
            }
        });*/
        double pre_network_longitude = Integer.MAX_VALUE;
        double pre_network_latitude = Integer.MAX_VALUE;
        double pre_gps_longitude = Integer.MAX_VALUE;
        double pre_gps_latitude = Integer.MAX_VALUE;
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
                    0, locationListeners[0]);
        } catch (IllegalArgumentException e) {
            Log.d(LOG_TAG, "network provider does not exist, " + e.getMessage());
        } catch (SecurityException e) {
            Log.i(LOG_TAG, "fail to request location update, ignore", e);
        }

        timer = System.currentTimeMillis();
        //while (checkS_SentStatus(Data.db) || checkD_SentStatus(Data.db))
        while (checkD_SentStatus(Data.db) || checkS_SentStatus(Data.db)) {
            int i = 0;
            while (i != Data.db.size()) {
                long curTime = System.currentTimeMillis();
                if (curTime - timer >= REFRESH_TIME) {
                    timer = 0;
                    reset(Data.db);
                }
                //requestLoc();



                //Items tmp = items.get(i);

                if (checkD_SentStatus(Data.db)) {
                    if (compareDueTime(Data.db.get(i)) > 0) {
                        Log.d(LOG_TAG, "Item on time");
                        if (manager != null) {
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
                            loc = manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (loc != null) {
                                if (pre_network_latitude == Integer.MAX_VALUE && pre_network_longitude == Integer.MAX_VALUE) {
                                    pre_network_latitude = loc.getLatitude();
                                    pre_network_longitude = loc.getLongitude();
                                }
                                if (cur_latitude != pre_network_latitude && cur_longitude != pre_network_longitude) {
                                    cur_latitude = loc.getLatitude();
                                    cur_longitude = loc.getLongitude();
                                }
                            }
                        }
                        if (comparePlace(home)) {
                            if (compareDate(Data.db.get(i))) {
                                sms.sendTextMessage(myPhoneNo, null, "Need to finish " + items.get(i).getTxt() + " today.", null, null);
                                Toast toast = Toast.makeText(this.getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT);
                                toast.show();
                                //items.remove(tmp);
                                Data.db.get(i).setS_Sent();
                                Data.db.get(i).setD_Sent();
                            }
                        }
                        if (manager != null) {
                            loc = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (loc != null) {
                                if (pre_gps_latitude == Integer.MAX_VALUE && pre_gps_longitude == Integer.MAX_VALUE) {
                                    pre_gps_latitude = loc.getLatitude();
                                    pre_gps_longitude = loc.getLongitude();
                                }
                                if (cur_latitude != pre_gps_latitude && cur_longitude != pre_gps_longitude) {
                                    cur_latitude = loc.getLatitude();
                                    cur_longitude = loc.getLongitude();
                                }
                            }
                        }
                        if (comparePlace(home)) {
                            if (compareDate(Data.db.get(i))) {
                                sms.sendTextMessage(myPhoneNo, null, "Need to finish " + items.get(i).getTxt() + " today.", null, null);
                                Toast toast = Toast.makeText(this.getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT);
                                toast.show();
                                //items.remove(tmp);
                                Data.db.get(i).setS_Sent();
                                Data.db.get(i).setD_Sent();
                            }
                        }

                    } else if (compareDueTime(Data.db.get(i)) <= 0) {
                        //items.remove(tmp);
                        Log.d(LOG_TAG, "Item due");
                        Data.db.get(i).setD_Sent();
                    }

                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                    }
                }

                if (checkS_SentStatus(Data.db)) {
                    if (compareTime(Data.db.get(i)) == 0) {
                        Log.d(LOG_TAG, "Item on time");
                        sms.sendTextMessage(myPhoneNo, null, "It is time to do " + Data.db.get(i).getTxt(), null, null);
                        Toast toast = Toast.makeText(this.getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT);
                        toast.show();
                        Data.db.get(i).setS_Sent();
                        //items.remove(tmp);
                    } else if (compareTime(Data.db.get(i)) < 0) {
                        //items.remove(tmp);
                        Data.db.get(i).setS_Sent();
                        Log.d(LOG_TAG, "Item deleted");

                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                    }
                }
                i++;
            }
        }
    }


    private void reset(ArrayList<Items> db) {
        for (int i = 0; i < db.size(); i++) {
            db.get(i).resetD_Sent();
            db.get(i).resetS_Sent();
        }
    }

    private boolean checkD_SentStatus(ArrayList<Items> itemses) {
        boolean[] dp = new boolean[itemses.size() + 1];
        dp[0] = true;
        for (int i = 1; i <= itemses.size(); i++) {
            dp[i] = dp[i-1] & itemses.get(i - 1).getD_Sent();
            Log.d("Sent status", Boolean.toString(dp[i]));

        }
        Log.d("Sent status", Boolean.toString(dp[itemses.size()]));
        return !dp[itemses.size()];
    }

    private boolean checkS_SentStatus(ArrayList<Items> itemses) {
        boolean[] dp = new boolean[itemses.size() + 1];
        dp[0] = true;
        for (int i = 1; i <= itemses.size(); i++) {
            dp[i] = dp[i-1] & itemses.get(i - 1).getS_Sent();
            Log.d("Sent status", Boolean.toString(dp[i]));

        }
        Log.d("Sent status", Boolean.toString(dp[itemses.size()]));
        return !dp[itemses.size()];
    }


    private boolean compareDate(Items it) {
        DateFormat df = new SimpleDateFormat("MM dd yyyy HH mm");
        String date = df.format(Calendar.getInstance().getTime());
        String[] strs = date.split(" ");
        int mm = Integer.parseInt(strs[0]);
        int dd = Integer.parseInt(strs[1]);
        int yy = Integer.parseInt(strs[2]);
        int hh = Integer.parseInt(strs[3]);
        int min = Integer.parseInt(strs[4]);

        if (yy == it.getS_y()) {
            if (mm == it.getS_m()) {
                if (dd == it.getS_d()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Compare item time with system time.
     * @param it
     * @return
     */
    private int compareTime(Items it) {
        DateFormat df = new SimpleDateFormat("MM dd yyyy HH mm");
        String date = df.format(Calendar.getInstance().getTime());
        String[] strs = date.split(" ");
        int mm = Integer.parseInt(strs[0]);
        int dd = Integer.parseInt(strs[1]);
        int yy = Integer.parseInt(strs[2]);
        int hh = Integer.parseInt(strs[3]);
        int min = Integer.parseInt(strs[4]);

        if (yy == it.getS_y()) {
            if (mm == it.getS_m()) {
                if (dd == it.getS_d()) {
                    if (hh == it.getS_h()) {
                        if (min == it.getS_minu()) {
                            return 0;
                        } else if (min > it.getS_minu()) {
                            return -1;
                        } else { return 1; }
                    } else if (hh > it.getS_h()) { return -1;}
                    else { return 1; }
                } else if (dd > it.getS_d()) { return -1; }
                else { return 1; }
            } else if (mm > it.getS_m()) { return -1; }
            else { return 1; }
        } else if (yy > it.getS_y()) { return -1; }
        else return 1;
    }

    private int compareDueTime(Items it) {
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

    private boolean comparePlace(LatLng home) {
        double lat_meter = 0.0000449;
        double lon_meter = lat_meter/Math.cos(home.latitude);
        Log.d("GPS Current Location", Double.toString(cur_latitude) + ", " + Double.toString(cur_longitude));
        Log.d("GPS Home Location", Double.toString(home.latitude) + ", " + Double.toString(home.longitude));

        if (Math.abs(cur_latitude) <= Math.abs(home.latitude + lat_meter) &&
                Math.abs(cur_latitude) >= Math.abs(home.latitude - lat_meter)) {
            Log.d("Compare Place", "Latitude match");
            if (Math.abs(cur_longitude - home.longitude) <= lon_meter) {
                Log.d("Compare Place", "Current place is same as home location");
                return true;
            }
        }
        Log.d("Compare Place", "Current place is different from home location");
        return false;
    }

    private void initLocationManger() {
        if (manager == null) {
            manager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }


    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void restart(ArrayList<Items> items) {
        Collections.sort(items, new Comparator<Items>() {
            @Override
            public int compare(Items it1, Items it2) {
                return it1.getStart_date().compareTo(it2.getStart_date());
            }
        });


    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(LOG_TAG, "Service Created.");
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.getApplicationContext(),"SMS Service created.", duration);
        toast.show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.d(LOG_TAG, "Service Destroyed.");
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(this.getApplicationContext(),"SMS Service destroyed.", duration);
        toast.show();

    }


}


