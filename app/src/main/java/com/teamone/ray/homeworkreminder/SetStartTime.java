package com.teamone.ray.homeworkreminder;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;

public class SetStartTime extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_start_time);
        final Intent intent = getIntent();

        final NumberPicker month = (NumberPicker)findViewById(R.id.month);
        final NumberPicker day = (NumberPicker)findViewById(R.id.day);
        final NumberPicker year = (NumberPicker) findViewById(R.id.year);
        final NumberPicker hr = (NumberPicker) findViewById(R.id.hour);
        final NumberPicker minute = (NumberPicker) findViewById(R.id.minute);

        Bundle b = intent.getExtras();

        month.setMaxValue(12);
        month.setMinValue(1);
        month.setValue(b.getInt("d_m"));
        day.setMaxValue(31);
        day.setMinValue(1);
        day.setValue(b.getInt("d_d"));
        int yy = b.getInt("d_y");
        year.setMaxValue(yy + 20);
        year.setMinValue(yy - 1);
        year.setValue(yy);
        hr.setMaxValue(23);
        hr.setMinValue(0);
        hr.setValue(b.getInt("d_h") - 1);
        minute.setMaxValue(59);
        minute.setMinValue(0);
        minute.setValue(b.getInt("d_minu"));

        Button button = (Button)findViewById(R.id.button);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int m = month.getValue();
                int d = day.getValue();
                int y = year.getValue();
                int h = hr.getValue();
                int minu = minute.getValue();

                Bundle b = new Bundle();
                b.putInt("s_m", m);
                b.putInt("s_d", d);
                b.putInt("s_y", y);
                b.putInt("s_h", h);
                b.putInt("s_minu", minu);

                intent.putExtras(b);

                setResult(2, intent);
                finish();
            }
        });
    }
}
