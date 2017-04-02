package com.teamone.ray.homeworkreminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SetMessage extends AppCompatActivity {
    Intent intent;
    NumberPicker month;
    NumberPicker day;
    NumberPicker year;
    NumberPicker hr;
    NumberPicker minute;
    EditText editText;
    Bundle b;
    public String INI_TXT = "Click here to add your first reminder.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_message);

        intent = getIntent();

        month = (NumberPicker)findViewById(R.id.month);
        day = (NumberPicker)findViewById(R.id.day);
        year = (NumberPicker) findViewById(R.id.year);
        hr = (NumberPicker)findViewById(R.id.hour);
        minute = (NumberPicker)findViewById(R.id.minute);

        int mm = 0, dd = 0, yy = 0, hh = 0, min = 0;

        Bundle bd = intent.getExtras();
        b = new Bundle();

        String msg;

        if (bd == null || bd.getInt("d_m") == 0) {
            DateFormat df = new SimpleDateFormat("MM dd yyyy HH mm");
            String date = df.format(Calendar.getInstance().getTime());
            String[] strs = date.split(" ");
            mm = Integer.parseInt(strs[0]);
            dd = Integer.parseInt(strs[1]);
            yy = Integer.parseInt(strs[2]);
            hh = Integer.parseInt(strs[3]);
            min = Integer.parseInt(strs[4]);
            msg = "Set your assignment title";
        } else {
            b.putInt("pos", bd.getInt("pos") );
            mm = bd.getInt("d_m");
            dd = bd.getInt("d_d");
            yy = bd.getInt("d_y");
            hh = bd.getInt("d_h");
            min = bd.getInt("d_minu");

            msg = bd.getString("msg");
            if (msg.equals(INI_TXT)) {
                msg = "Set your assignment title";
            }
        }

        if (yy == 0) {
            Calendar calendar = Calendar.getInstance();

            DateFormat df = new SimpleDateFormat("MM dd yyyy HH mm");
            String date = df.format(Calendar.getInstance().getTime());
            String[] strs = date.split(" ");
            yy = Integer.parseInt(strs[2]);
        }
        month.setMaxValue(12);
        month.setMinValue(1);
        month.setValue(mm);
        day.setMaxValue(31);
        day.setMinValue(1);
        day.setValue(dd);
        year.setMaxValue(yy + 20);
        year.setMinValue(yy - 1);
        year.setValue(yy);
        hr.setMaxValue(23);
        hr.setMinValue(0);
        hr.setValue(hh);
        minute.setMaxValue(59);
        minute.setMinValue(0);
        minute.setValue(min);

        editText = (EditText)findViewById(R.id.editText);

        editText.setText(msg);


        Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                receiveData(view);
            }
        });
    }

    private void receiveData(View view) {
        Intent intent1 = new Intent(this, SetStartTime.class);

        int m = month.getValue();
        int d = day.getValue();
        int y = year.getValue();
        int h = hr.getValue();
        int minu = minute.getValue();
        String msg = editText.getText().toString();

        b.putInt("d_m", m);
        b.putInt("d_d", d);
        b.putInt("d_y", y);
        b.putInt("d_h", h);
        b.putInt("d_minu", minu);
        b.putString("msg", msg);

        intent1.putExtras(b);

        startActivityForResult(intent1, 1);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == 2) {
            if (requestCode == 1) {
                Bundle bundle = data.getExtras();
                b.putInt("s_m", bundle.getInt("s_m"));
                b.putInt("s_d", bundle.getInt("s_d"));
                b.putInt("s_y", bundle.getInt("s_y"));
                b.putInt("s_h", bundle.getInt("s_h"));
                b.putInt("s_minu", bundle.getInt("s_minu"));
                intent.putExtras(b);

                setResult(3, intent);
                finish();
            }
        }
    }
}
