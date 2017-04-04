package com.vitaliyhtc.speedometerview;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    private SpeedometerView mSpeedometerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSpeedometerView = (SpeedometerView) findViewById(R.id.SpeedometerView);
        mSpeedometerView.setArrowAccelerationSpeed(1.0f);
        mSpeedometerView.setArrowAttenuationSpeed(0.1f);
        mSpeedometerView.setEnergyLevelChangeSpeed(0.3f);
        mSpeedometerView.setEnergyLevel(100.0f);
        setOnClickListeners();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mSpeedometerView.switchOn();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSpeedometerView.switchOff();
    }

    private void setOnClickListeners(){
        Button brakeButton = (Button) findViewById(R.id.btn_brake);
        Button trottleButton = (Button) findViewById(R.id.btn_trottle);

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


    }





}
