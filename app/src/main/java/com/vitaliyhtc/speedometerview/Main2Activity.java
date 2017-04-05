package com.vitaliyhtc.speedometerview;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Main2Activity extends AppCompatActivity {

    private SpeedometerView mSpeedometerView;
    private TextView mSpeedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        RelativeLayout mainRelativeLayout = (RelativeLayout) findViewById(R.id.rl_main);

        mSpeedTextView = (TextView) findViewById(R.id.tv_speed);

        //mSpeedometerView = (SpeedometerView) findViewById(R.id.SpeedometerView);

        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);
        params.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);

        mSpeedometerView = new SpeedometerView(getApplicationContext());
        mSpeedometerView.setOuterCircleColor(0xffff5722);

        mainRelativeLayout.addView(mSpeedometerView, params);

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setOnClickListeners(){
        Button brakeButton = (Button) findViewById(R.id.btn_brake);
        Button trottleButton = (Button) findViewById(R.id.btn_trottle);
        Button fillWithFuelButton = (Button) findViewById(R.id.btn_add_fuel);

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
    }

    private void setOnSpeedChangeListener(){
        mSpeedometerView.setOnSpeedChangeListener(new SpeedometerView.SpeedChangeListener() {
            @Override
            public void onSpeedChanged(int value) {
                String speedText = ""+value;
                mSpeedTextView.setText(speedText);
            }
        });
    }

}
