package com.teamone.ray.homeworkreminder;

import android.app.IntentService;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

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

    public static final String CREATE = "CREATE";
    public static final String RESTART = "DESTROY";
    public static final String LOG_TAG = "Alarm_Service";

    public static final String EXTRA_PARAM1 = "items";
    public static final String EXTRA_PARAM2 = "com.teamone.ray.homeworkreminder.extra.PARAM2";

    public Alarm_Service() {
        super("Alarm_Service");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.d(LOG_TAG, "Service Started.");
        if (intent != null) {
            final String action = intent.getAction();
            if (CREATE.equals(action)) {
                Bundle b = intent.getExtras();

                final ArrayList<Items> items = b.getParcelableArrayList(EXTRA_PARAM1);
                create(items);

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
    private void create(ArrayList<Items> items) {
       /* Collections.sort(items, new Comparator<Items>() {
            @Override
            public int compare(Items it1, Items it2) {
                return it1.getStart_date().compareTo(it2.getStart_date());
            }
        });*/

        TelephonyManager telephonyManager = (TelephonyManager) this.getApplicationContext().getSystemService(Context.TELEPHONY_SERVICE);
        String myPhoneNo = telephonyManager.getLine1Number();
        SmsManager sms = SmsManager.getDefault();

        while (!items.isEmpty()) {
            int i = 0;
            while (i != items.size()) {
                Items tmp = items.get(i);

                if (compareTime(tmp) == 0) {
                    sms.sendTextMessage(myPhoneNo, null, "It is time to do " + tmp.getTxt(), null, null);
                    Toast toast = Toast.makeText(this.getApplicationContext(), "SMS sent", Toast.LENGTH_SHORT);
                    toast.show();
                    items.remove(tmp);
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


