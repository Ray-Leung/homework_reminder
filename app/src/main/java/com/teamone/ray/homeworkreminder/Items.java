package com.teamone.ray.homeworkreminder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by ray on 3/23/2017.
 */

public class Items implements Parcelable{
    public String INI_TXT = "Click here to add your first reminder.";
    private String due_date;
    private String due_time;
    private String txt;
    private String start_date;
    private String start_time;
    private int d_m = 0;
    private int d_d = 0;
    private int d_y = 0;
    private int d_minu = 0;
    private int d_h = 0;
    private int s_m = 0;
    private int s_d = 0;
    private int s_y = 0;
    private int s_minu = 0;
    private int s_h = 0;
    private boolean s_sent = false;
    private boolean d_sent =false;

    public Items() {
        due_date = "";
        due_time = "";
        start_date = due_date;
        start_time = due_time;
        txt = INI_TXT;
    }

    public Items(String s) {
        String[] strs = s.split("`");
        d_m = Integer.parseInt(strs[0]);
        d_d = Integer.parseInt(strs[1]);
        d_y = Integer.parseInt(strs[2]);
        d_h = Integer.parseInt(strs[3]);
        d_minu = Integer.parseInt(strs[4]);
        this.s_m = Integer.parseInt(strs[5]);
        this.s_d = Integer.parseInt(strs[6]);
        this.s_y = Integer.parseInt(strs[7]);
        this.s_h = Integer.parseInt(strs[8]);
        this.s_minu = Integer.parseInt(strs[9]);
        this.txt = strs[10];

        due_date = Months.months[d_m - 1] + " " + String.valueOf(d_d) + " " + String.valueOf(d_y);
        String tmp = d_minu > 10? String.valueOf(d_minu) : "0" +
                String.valueOf(d_minu);
        due_time = String.valueOf(d_h) + " : " + tmp;

        tmp = s_m > 10 ? String.valueOf(s_m) : "0" + String.valueOf(s_m);
        start_date = String.valueOf(s_y) + " " + s_m + " " + (s_d > 10 ? String.valueOf(s_d) : "0" + String.valueOf(s_d));
        tmp = s_minu > 10? String.valueOf(s_minu) : "0" +
                String.valueOf(s_minu);
        start_time = (s_h > 10? String.valueOf(s_h) : "0" + String.valueOf(s_h)) + " " + tmp;
    }

    public Items(int m, int d, int y, int hr, int min, String txt,
                 int s_m, int s_d, int s_y, int s_hr, int s_min) {
        due_date = Months.months[m - 1] + " " + String.valueOf(d) + " " + String.valueOf(y);
        String tmp = min > 10? String.valueOf(min) : "0" +
                String.valueOf(min);
        due_time = String.valueOf(hr) + " : " + tmp;

        tmp = s_m > 10 ? String.valueOf(s_m) : "0" + String.valueOf(s_m);
        start_date = String.valueOf(s_y) + " " + s_m + " " + (s_d > 10 ? String.valueOf(s_d) : "0" + String.valueOf(s_d));
        tmp = s_min > 10? String.valueOf(s_min) : "0" +
                String.valueOf(s_min);
        start_time = (s_hr > 10? String.valueOf(s_hr) : "0" + String.valueOf(s_hr)) + " " + tmp;
        this.txt = txt;

        d_m = m;
        d_d = d;
        d_y = y;
        d_h = hr;
        d_minu = min;
        this.s_m = s_m;
        this.s_d = s_d;
        this.s_y = s_y;
        this.s_h = s_hr;
        this.s_minu = s_min;
    }

    public Items(Parcel p) {
        int[] data = new int[10];

        p.readIntArray(data);
        this.txt = p.readString();

        this.d_m = data[0];
        this.d_d = data[1];
        this.d_y = data[2];
        this.d_h = data[3];
        this.d_minu = data[4];
        this.s_m = data[5];
        this.s_d = data[6];
        this.s_y = data[7];
        this.s_h = data[8];
        this.s_minu = data[9];

        due_date = Months.months[d_m - 1] + " " + String.valueOf(d_d) + " " + String.valueOf(d_y);
        String tmp = d_minu > 10? String.valueOf(d_minu) : "0" +
                String.valueOf(d_minu);
        due_time = String.valueOf(d_h) + " : " + tmp;

        tmp = s_m > 10 ? String.valueOf(s_m) : "0" + String.valueOf(s_m);
        start_date = String.valueOf(s_y) + " " + s_m + " " + (s_d > 10 ? String.valueOf(s_d) : "0" + String.valueOf(s_d));
        tmp = s_minu > 10? String.valueOf(s_minu) : "0" +
                String.valueOf(s_minu);
        start_time = (s_h > 10? String.valueOf(s_h) : "0" + String.valueOf(s_h)) + " " + tmp;

    }

    public void modify(int m, int d, int y, int hr, int min, String txt,
                       int s_m, int s_d, int s_y, int s_hr, int s_min) {
        d_sent = false;
        s_sent = false;
        due_date = Months.months[m - 1] + " " + String.valueOf(d) + " " + String.valueOf(y);
        String tmp = min > 10? String.valueOf(min) : "0" +
                String.valueOf(min);
        due_time = String.valueOf(hr) + " : " + tmp;
        this.txt = txt;

        tmp = s_m > 10 ? String.valueOf(s_m) : "0" + String.valueOf(s_m);
        start_date = String.valueOf(s_y) + " " + s_m + " " + (s_d > 10 ? String.valueOf(s_d) : "0" + String.valueOf(s_d));
        tmp = s_min > 10? String.valueOf(s_min) : "0" +
                String.valueOf(s_min);
        start_time = (s_hr > 10? String.valueOf(s_hr) : "0" + String.valueOf(s_hr)) + " " + tmp;

        d_m = m;
        d_d = d;
        d_y = y;
        d_h = hr;
        d_minu = min;
        this.s_m = s_m;
        this.s_d = s_d;
        this.s_y = s_y;
        this.s_h = s_hr;
        this.s_minu = s_min;
    }

    public String getDue_date() {
        return this.due_date;
    }

    public String getDue_time() {
        return this.due_time;
    }

    public String getStart_date() { return this.start_date; }

    public String getStart_time() { return this.start_time; }

    public int getD_m() { return d_m; }

    public int getD_d() { return d_d; }

    public int getD_y() { return d_y; }

    public int getD_minu() { return d_minu; }

    public int getD_h() { return d_h; }

    public int getS_m() { return s_m; }

    public int getS_d() { return s_d; }

    public int getS_y() { return s_y; }

    public int getS_minu() { return s_minu; }

    public int getS_h() { return s_h; }

    public String getTxt() { return this.txt; }

    public void setS_Sent() {
        if (s_sent == false) s_sent = true;
    }

    public void resetS_Sent() { s_sent = false; }

    public boolean getS_Sent() { return s_sent; }

    public void setD_Sent() {
        if (d_sent == false) d_sent = true;
    }

    public void resetD_Sent() { d_sent = false; }

    public boolean getD_Sent() { return d_sent; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeIntArray(new int[] {this.d_m, this.d_d, this.d_y, this.d_h,
        this.d_minu, this.s_m, this.s_d, this.s_y, this.s_h, this.s_minu });
        parcel.writeString(this.txt);
    }

    public static final Parcelable.Creator CREATOR = new Creator<Items>() {
        @Override
        public Items createFromParcel(Parcel parcel) {
            return new Items(parcel);
        }

        @Override
        public Items[] newArray(int i) {
            return new Items[i];
        }
    };
}
