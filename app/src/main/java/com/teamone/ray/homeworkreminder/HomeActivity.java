package com.teamone.ray.homeworkreminder;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.amlcurran.showcaseview.ShowcaseView;
import com.github.amlcurran.showcaseview.targets.ActionViewTarget;
import com.github.amlcurran.showcaseview.targets.Target;
import com.github.amlcurran.showcaseview.targets.ViewTarget;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

import static com.teamone.ray.homeworkreminder.Data.db;


/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class HomeActivity extends AppCompatActivity {

    private Boolean FIRST_LAUNCH = null;
    private final static int PERMISSION_REQUEST_CODE = 1;
    private final static int ITEM_DATA_RESULT = 3;
    private final static int PLACE_DATA_REQUEST = 1;
    private final static int ITEM_MODIFY_REQUEST = 2;
    private final static int ITEM_DATA_REQUEST = 1;
    private final static int TIME_LIMIT = 1500;
    private static long timeBackPressed;
    private LatLng lng;
    private static double home_longitude;
    private static double home_latitude;

    ListView listView;
    CustomListAdapter cla;
    ShowcaseView sv;
    private Target t_map;
    private Target t_add;
    private Target t_mot;
    private Target t_del;

    private int helper_cnt = 0;

    private static final String[] PERMISSIONS_REQUIRED = { Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE };
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
           //hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);

        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        findViewById(R.id.dummy_button).setOnTouchListener(mDelayHideTouchListener);
        final Button button = (Button) findViewById(R.id.dummy_button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveData(view);
            }
        });

        if (db.isEmpty()) {
            Data.read_data(this, db);
        }
        lng = Data.read_loc(this);

        listView = (ListView)findViewById(R.id.listView);
        cla = new CustomListAdapter(this, db);
        listView.setAdapter(cla);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                modifyData(adapterView, view, i, l);
            }
        });

        t_map = new Target() {
            @Override
            public Point getPoint() {
                Display display = getWindowManager().getDefaultDisplay();
                Point dis = new Point();
                display.getSize(dis);
                int width = dis.x;
                int height = dis.y;
                return new Point(width - 100 , height/2 - height/3 - height/15 - 44);
            }
        };
        t_del = new Target() {
            @Override
            public Point getPoint() {
                Display display = getWindowManager().getDefaultDisplay();
                Point dis = new Point();
                display.getSize(dis);
                int width = dis.x;
                int height = dis.y;
                return new Point(width - 100 , height/2 - height/3);
            }
        };

        t_add = new ViewTarget(R.id.dummy_button, this);
        t_mot = new ViewTarget(R.id.listView, this);
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        sv = new ShowcaseView.Builder(this)
                .withHoloShowcase()
                .setTarget(Target.NONE)
                .setContentTitle("Access Permissions")
                .setContentText("Access all permissions below ")
                .build();
        sv.setButtonPosition(params);
        sv.setButtonText("Continue");
        sv.overrideButtonClick(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCaseOnClick(v);
            }
        });

        sv.hide();

        if (isFirstLaunch()) {
            helper();
        }

        if (lng != null)
            Log.d("Home Location", lng.toString());
        checkPermissions();


    }

    private void helper() {

        show();
        sv.show();
    }


    private void showCaseOnClick(View view) {
        switch (helper_cnt){
            case 0:
                sv.setShowcase(t_map, true);
                sv.setContentTitle("Home Location");
                sv.setContentText("Set you home location by clicking the button on the top-right corner");
                helper_cnt++;
                break;
            case 1:
                sv.setShowcase(t_mot, true);
                sv.setContentTitle("Modify Assignment");
                sv.setContentText("Click item to modify assignment");
                helper_cnt++;
                break;
            case 2:
                sv.setShowcase(t_del, true);
                sv.setContentTitle("Delete Assignment");
                sv.setContentText("Click Delete Button to delete assignment");
                helper_cnt++;
            case 3:
                sv.setShowcase(t_add, true);
                sv.setContentTitle("Add Assignment");
                sv.setContentText("Click the add button to add new assignment");
                sv.setButtonText("Done");
                helper_cnt++;
                break;
            case 4:
                sv.hide();
                sv.setButtonText("Continue");
                sv.setShowcase(Target.NONE, true);
                sv.setContentTitle("Access Permissions");
                sv.setContentText("Access all permissions below ");
                helper_cnt = 0;
                break;
        }
    }

    private void modifyData(AdapterView<?> adapterView, View view, int i, long l) {
        checkPermissions();
        Items item = db.get(i);
        Intent intent = new Intent(this, SetMessage.class);
        Bundle b = new Bundle();
        Data.collectData(b, item, i);

        intent.putExtras(b);
        startActivityForResult(intent, ITEM_MODIFY_REQUEST);

    }

    private void receiveData(View view) {
        checkPermissions();
        Intent intent = new Intent(this, SetMessage.class);
        startActivityForResult(intent, ITEM_DATA_REQUEST);
    }

    private boolean isFirstLaunch() {
        if (FIRST_LAUNCH == null) {
            SharedPreferences sp = this.getSharedPreferences("first_time", Context.MODE_PRIVATE);
            FIRST_LAUNCH = sp.getBoolean("firstTime", true);
            if (FIRST_LAUNCH) {
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("firstTime", false);
                editor.commit();
            }
        }
        return FIRST_LAUNCH;
    }


    /**
     * Check my service if is running
     * @param serviceClass
     * @return
     */
    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);

        for (ActivityManager.RunningServiceInfo serviceInfo : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(serviceInfo.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if all permissions are granted
     */
    private void checkPermissions() {
        final ArrayList<String> missedPermissions = new ArrayList<>();

        for (final String permission : PERMISSIONS_REQUIRED) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missedPermissions.add(permission);
            }
        }

        if (!missedPermissions.isEmpty()) {
            final String[] permissions = new String[missedPermissions.size()];
            missedPermissions.toArray(permissions);

            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.map:
                checkPermissions();
                //Intent intent = new Intent(this, MapsActivity.class);
                //startActivity(intent);
                PlacePicker.IntentBuilder picker = new PlacePicker.IntentBuilder();
                try {
                    startActivityForResult(picker.build(this), PLACE_DATA_REQUEST) ;
                    return true;
                } catch (GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            case R.id.helper:
                helper();
                return true;

        }
        return false;

    }

    @Override
    public void onRequestPermissionsResult(final int requestCode,  final String[] permissions,
                                           final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        String TAG = "onRequestPermissionsResult";
        if (requestCode == PERMISSION_REQUEST_CODE) {
            int i = 0;

            for (final int res : grantResults) {
                if (res != PackageManager.PERMISSION_GRANTED) {
                    if (i < permissions.length) {
                        Log.e(TAG, "failed to permission: " + permissions[i]);
                        throw new Error("Failed to Request permission.", null);
                    } else {
                        Log.e(TAG, "failed to permission: Out of Range.");
                        throw new Error("Failed to Request permission.", null);
                    }
                }
                i++;
            }
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == ITEM_DATA_RESULT) {
            //Intent serv = new Intent(this, Alarm_Service.class);
            if (requestCode == ITEM_DATA_REQUEST) {
                Bundle b = data.getExtras();
                db.add(new Items(b.getInt("d_m"), b.getInt("d_d"), b.getInt("d_y"),
                        b.getInt("d_h"), b.getInt("d_minu"), b.getString("msg"),
                        b.getInt("s_m"), b.getInt("s_d"), b.getInt("s_y"),
                        b.getInt("s_h"), b.getInt("s_minu")));
                cla.notifyDataSetChanged();
                Data.save(this ,db);

            } else if (requestCode == ITEM_MODIFY_REQUEST) {
                Bundle b = data.getExtras();

                db.get(b.getInt("pos")).modify(b.getInt("d_m"), b.getInt("d_d"), b.getInt("d_y"),
                        b.getInt("d_h"), b.getInt("d_minu"), b.getString("msg"),
                        b.getInt("s_m"), b.getInt("s_d"), b.getInt("s_y"),
                        b.getInt("s_h"), b.getInt("s_minu"));

                cla.notifyDataSetChanged();
                Data.save(this ,db);
            }

        } else if(resultCode == RESULT_OK) {
            if (requestCode == PLACE_DATA_REQUEST) {
                Place place = PlacePicker.getPlace(this, data);
                lng = place.getLatLng();
                if (lng != null)
                    Log.d("Home Location", lng.toString());
                Data.save_home(this, lng);
                home_longitude = lng.longitude;
                home_latitude = lng.latitude;
                String address = place.getAddress().toString();

                Log.d("Place call back", address);
                Log.d("Position call back", Double.toString(home_latitude) + " , " +
                        Double.toString(home_longitude));

                cla.notifyDataSetChanged();

            }
        }

    }


    @Override
    public void onBackPressed() {
        if (TIME_LIMIT + timeBackPressed > System.currentTimeMillis()) {
            super.onBackPressed();
        } else {
            Toast.makeText(this, "Press back again to exit", Toast.LENGTH_SHORT).show();
            show();
        }
        timeBackPressed = System.currentTimeMillis();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(3500);
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    private class CustomListAdapter extends BaseAdapter {
        private ArrayList<Items> listData;
        private LayoutInflater layoutInflater;

        public CustomListAdapter(Context aContext, ArrayList<Items> listData) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(aContext);
        }

        @Override
        public void notifyDataSetChanged() {
            super.notifyDataSetChanged();
            Intent serv = new Intent(HomeActivity.this, Alarm_Service.class);
            serv.setAction("CREATE");
            //serv.putParcelableArrayListExtra("items", db);
            serv.putExtra("place", lng);
            if (!isMyServiceRunning(Alarm_Service.class))
                startService(serv);
            else {
                stopService(serv);
                startService(serv);
            }
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(final int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.list_section_layout, null);
                holder = new ViewHolder();
                holder.dueView = (TextView) convertView.findViewById(R.id.due);
                holder.txtView = (TextView) convertView.findViewById(R.id.txt);
                holder.delButton = (Button) convertView.findViewById(R.id.button);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            String due;
            if (listData.get(position).getDue_date().isEmpty()) {
                due = "";
            }
            else {
                due = "Due:" + listData.get(position).getDue_date() + " " +
                        listData.get(position).getDue_time();
            }

            String d_t;

            if (listData.get(position).getTxt().isEmpty()) {
                d_t = "";
            } else  {
                d_t = listData.get(position).getTxt();
            }

            holder.txtView.setText(due);
            holder.dueView.setText(d_t);

            //holder.delButton.setTag(position);
            holder.delButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AlertDialog adl = new AlertDialog.Builder(HomeActivity.this)
                            .setTitle("Delete entry")
                            .setMessage("Are you sure you want to delete this reminder?")
                            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // continue with delete

                                    db.remove(position);
                                    notifyDataSetChanged();
                                    Data.save(HomeActivity.this, db);

                                }
                            })
                            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                }
                            })
                            .setIcon(android.R.drawable.ic_menu_delete)
                            .show();
                }
            });

            return convertView;
        }

        private class ViewHolder {
            TextView txtView;
            TextView dueView;
            Button delButton;
        }
    }
}