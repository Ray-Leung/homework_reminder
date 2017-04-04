package com.teamone.ray.homeworkreminder;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 * Created by ray on 3/23/2017.
 */
public class Data {
    public static ArrayList<Items> db = new ArrayList<>();


    public static void read_data(Activity activity,ArrayList<Items> db)  {
        String FILENAME = "db.dat";
        Context context = activity.getApplicationContext();
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            String line;
            if (fis != null) {
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bf = new BufferedReader(isr);
                while ((line = bf.readLine()) != null) {

                    Items item = new Items(line);
                    db.add(item);
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
            db.add(new Items());
        }

    }

    // Save User data
    public static void save(Activity activity, ArrayList<Items> db) {
        String filename = "db.dat";
        Context context = activity.getApplicationContext();
        try(OutputStreamWriter bw = new OutputStreamWriter(context.openFileOutput(filename,
                Context.MODE_PRIVATE))) {
            String res = buildString(db);
            bw.write(res);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static LatLng read_loc(Activity activity)  {
        String FILENAME = "loc.dat";
        Context context = activity.getApplicationContext();
        LatLng latLng = null;
        try {
            FileInputStream fis = context.openFileInput(FILENAME);
            String line;
            if (fis != null) {
                InputStreamReader isr = new InputStreamReader(fis);
                BufferedReader bf = new BufferedReader(isr);
                while ((line = bf.readLine()) != null) {
                    String[] strs = line.split(" ");
                    double latitude = Double.parseDouble(strs[0]);
                    double longitude = Double.parseDouble(strs[1]);
                    latLng = new LatLng(latitude, longitude);
                }
            }
            fis.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return latLng;
    }

    public static void save_home(Activity activity, LatLng latLng) {
        String filename = "loc.dat";
        Context context = activity.getApplicationContext();
        try(OutputStreamWriter bw = new OutputStreamWriter(context.openFileOutput(filename,
                Context.MODE_PRIVATE))) {
            String res = Double.toString(latLng.latitude) + " " + Double.toString(latLng.longitude);
            bw.write(res);
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String buildString(ArrayList<Items> db) {
        String res = "";

        for (Items items : db) {
            res += items.getD_m() + "`" + items.getD_d() + "`" +  items.getD_y() + "`" +
                    items.getD_h() + "`" + items.getD_minu() + "`" + items.getS_m() + "`" +
                    items.getS_d() + "`" + items.getS_y() + "`" + items.getS_h() + "`" +
                    items.getS_m() + "`" + items.getTxt() + "\n";
        }

        return res;
    }

    public static void collectData(Bundle b, Items i, int pos) {
        b.putInt("d_m", i.getD_m());
        b.putInt("d_d", i.getD_d());
        b.putInt("d_y", i.getD_y());
        b.putInt("d_h", i.getD_h());
        b.putInt("d_minu", i.getD_minu());
        b.putString("msg", i.getTxt());
        b.putInt("s_m", i.getS_m());
        b.putInt("s_d", i.getS_d());
        b.putInt("s_y", i.getS_y());
        b.putInt("s_h", i.getS_h());
        b.putInt("s_minu", i.getS_minu());
        b.putInt("pos", pos);
    }
}


