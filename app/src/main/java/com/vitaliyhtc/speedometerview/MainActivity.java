package com.vitaliyhtc.speedometerview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private SpeedometerView mSpeedometerView;
    private TextView mSpeedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpeedTextView = (TextView) findViewById(R.id.tv_speed);

        mSpeedometerView = (SpeedometerView) findViewById(R.id.SpeedometerView);
        mSpeedometerView.setArrowAccelerationSpeed(1.0f);
        mSpeedometerView.setArrowAttenuationSpeed(0.05f);
        mSpeedometerView.setEnergyLevelChangeSpeed(0.3f);
        mSpeedometerView.setEnergyLevel(100.0f);
        setOnClickListeners();
        setOnSpeedChangeListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mSpeedometerView.switchOn();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mSpeedometerView.switchOff();
    }



    private void setOnClickListeners(){
        Button brakeButton = (Button) findViewById(R.id.btn_brake);
        Button trottleButton = (Button) findViewById(R.id.btn_trottle);
        Button fillWithFuelButton = (Button) findViewById(R.id.btn_add_fuel);
        Button startSecondActivity = (Button) findViewById(R.id.btn_startSecondActivity);

        brakeButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction){
                    case MotionEvent.ACTION_DOWN:
                        mSpeedometerView.pressBrakePedal();
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSpeedometerView.releaseBrakePedal();
                        return true;
                }
                return false;
            }
        });

        trottleButton.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int eventAction = event.getAction();
                switch (eventAction){
                    case MotionEvent.ACTION_DOWN:
                        mSpeedometerView.pressTrottlePedal();
                        return true;
                    case MotionEvent.ACTION_UP:
                        mSpeedometerView.releaseTrottlePedal();
                        return true;
                }
                return false;
            }
        });

        fillWithFuelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeedometerView.setEnergyLevel(100);
            }
        });

        startSecondActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Main2Activity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            }
        });
    }

    private void setOnSpeedChangeListener(){
        mSpeedometerView.setOnSpeedChangeListener(new SpeedometerView.SpeedChangeListener() {
            @Override
            public void onSpeedChanged(int value) {
                // better to user String.valueOf(
                String speedText = String.valueOf(value);
                mSpeedTextView.setText(speedText);
            }
        });
    }

}
