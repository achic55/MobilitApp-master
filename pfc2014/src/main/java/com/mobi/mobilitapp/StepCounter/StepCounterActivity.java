package com.mobi.mobilitapp.StepCounter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import com.mobi.mobilitapp.R;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class StepCounterActivity extends Activity implements SensorEventListener {

    private SensorManager mSensorManager;
    private Sensor mSensor;
    private boolean isSensorPresent = false;
    private TextView stepsTodayText;
    private TextView caloriesTodayText;
    private TextView distanceTodayText;
    private TextView stepsTotalText;
    private TextView caloriesTotalText;
    private TextView distanceTotalText;
    private Switch switchStepCounter;
    private LinearLayout layoutStepCounter;
    private SharedPreferences prefs;
    int lastCounterStepsFromSensor;
    int lastCounterStepsFromToday;
    int lastCounterStepsTotal;
    boolean firstStep;
    NumberFormat formatterCalories;
    NumberFormat formatterDistance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_step_counter);

        prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE);

        stepsTodayText = (TextView) findViewById(R.id.stepTodayCounterText);
        caloriesTodayText = (TextView) findViewById(R.id.caloriesTodayCounterText);
        distanceTodayText = (TextView) findViewById(R.id.distanceTodayCounterText);

        stepsTotalText = (TextView) findViewById(R.id.stepTotalCounterText);
        caloriesTotalText = (TextView) findViewById(R.id.caloriesTotalCounterText);
        distanceTotalText = (TextView) findViewById(R.id.distanceTotalCounterText);

        switchStepCounter = (Switch) findViewById(R.id.switchStepCounter);
        layoutStepCounter = (LinearLayout) findViewById(R.id.linearLayoutStepCounter);

        formatterCalories = new DecimalFormat("#0.00");
        formatterDistance = new DecimalFormat("#0.000");

        mSensorManager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        if(mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER) != null)
        {

                mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

            String lastCounterStepsFromTodayString =
                    prefs.getString(PreferencesTypesStepCounter.todaySteps.toString(),"0");
            String lastCounterStepsTotalString =
                    prefs.getString(PreferencesTypesStepCounter.totalSteps.toString(),"0");
            try{
                lastCounterStepsFromToday = Integer.parseInt(lastCounterStepsFromTodayString);
                lastCounterStepsTotal = Integer.parseInt(lastCounterStepsTotalString);
            }
            catch (Exception e){
                lastCounterStepsFromToday = 0;
                lastCounterStepsTotal = 0;
            }

                firstStep = true;
                isSensorPresent = true;

            loadConfigurations();
        }
        else
        {
            isSensorPresent = false;
            dialogNoStepCounterHardware();
        }

        switchStepCounter.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener()
        {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                if(isChecked){
                    if(isSensorPresent){
                        layoutStepCounter.setVisibility(View.VISIBLE);
                        prefs.edit().putString(PreferencesTypesStepCounter.stepCounter.toString(),
                                PreferencesTypesStepCounter.on.toString()).apply();
                    }
                    else{
                        dialogNoStepCounterHardware();
                    }
                }
                else{
                    layoutStepCounter.setVisibility(View.INVISIBLE);
                    prefs.edit().putString(PreferencesTypesStepCounter.stepCounter.toString(),
                            PreferencesTypesStepCounter.off.toString()).apply();
                    if(isSensorPresent)
                    {
                        unregisterListenerSensor();
                    }
                }
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();

        if(isSensorPresent)
        {
            mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_FASTEST);
            loadConfigurations();
        }
    }

    private void unregisterListenerSensor(){
        if(isSensorPresent)
        {
            mSensorManager.unregisterListener(this);
        }
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int steps = (int) sensorEvent.values[0];

        if(firstStep){
            lastCounterStepsFromSensor = steps;
            firstStep = false;
        }

        int stepsCalibrated = steps - lastCounterStepsFromSensor;
        recalculateAndRefreshSteps(stepsCalibrated);
    }
    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    private void loadConfigurations() {
        String on = PreferencesTypesStepCounter.on.toString();
        String stringStepCounterOnOff = prefs.getString(PreferencesTypesStepCounter.stepCounter.toString(),
                PreferencesTypesStepCounter.off.toString());

        if(stringStepCounterOnOff.equalsIgnoreCase(on)){
            layoutStepCounter.setVisibility(View.VISIBLE);
            switchStepCounter.setChecked(true);

            loadData();
        }
        else{
            layoutStepCounter.setVisibility(View.INVISIBLE);
            switchStepCounter.setChecked(false);
        }
    }

    private void loadData(){
        String lastDataTime = prefs.getString(PreferencesTypesStepCounter.todayDataTime.toString(),
                PreferencesTypesStepCounter.noDataTime.toString());

        String todayDataTime = getTodayDate();

        if(!lastDataTime.equalsIgnoreCase(todayDataTime)){

            prefs.edit().putString(PreferencesTypesStepCounter.todaySteps.toString(),
                    "0").apply();
            prefs.edit().putString(PreferencesTypesStepCounter.todayDataTime.toString(),
                    todayDataTime).apply();

            lastCounterStepsFromToday = 0;
        }
        refreshValues();
    }

    private String getTodayDate(){
        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy");
        return df.format(c.getTime());
    }

    private void refreshValues(){
        String todayStepsValueString = prefs.getString(PreferencesTypesStepCounter.todaySteps.toString(), "0");
        int todayStepsValueInt = Integer.parseInt(todayStepsValueString);
        String totalStepsValueString = prefs.getString(PreferencesTypesStepCounter.totalSteps.toString(), "0");
        int totalStepsValueInt = Integer.parseInt(totalStepsValueString);

        refreshStepsValuesTotal(totalStepsValueInt);
        refreshStepsValuesToday(todayStepsValueInt);

    }

    public void resetStepData(View v){
        firstStep = true;
        lastCounterStepsFromToday = 0;
        lastCounterStepsTotal = 0;
        recalculateAndRefreshSteps(0);
    }

    private void dialogNoStepCounterHardware(){

        AlertDialog.Builder alert=new AlertDialog.Builder(this);
        alert.setTitle(getString(R.string.error)).setMessage(getString(R.string.pedometer_incompatible))
                .setNeutralButton(getString(R.string.acep), null).show();

        switchStepCounter.setChecked(false);
        layoutStepCounter.setVisibility(View.INVISIBLE);

    }

    private void recalculateAndRefreshSteps(int steps){
        int stepsTotal = steps + lastCounterStepsTotal;
        refreshStepsValuesTotal(stepsTotal);
        int stepsToday = steps + lastCounterStepsFromToday;
        refreshStepsValuesToday(stepsToday);
    }

    private void refreshStepsValuesToday(int steps){
        if(steps!=0){
            String stepStringValue = String.valueOf(steps);
            prefs.edit().putString(PreferencesTypesStepCounter.todaySteps.toString(), stepStringValue).apply();
            String stepsString = getString(R.string.steps)+": "+stepStringValue;
            stepsTodayText.setText(stepsString);
            Log.v("StepCounter", "refreshStepsValuesToday: steps = "+stepsString);

            double calories =  calculateCaloriesBySteps(steps);
            String caloriesStringValue = formatterCalories.format(calories);
            String caloriesString = getString(R.string.calories)+": "+caloriesStringValue;
            caloriesTodayText.setText(caloriesString);
            Log.v("StepCounter", "refreshStepsValuesToday: calories = "+caloriesString);

            double distance =  calculateKmBySteps(steps);
            String distanceStringValue = formatterDistance.format(distance);
            String distanceString = getString(R.string.distance)+": "+distanceStringValue+" km";
            distanceTodayText.setText(distanceString);
            Log.v("StepCounter", "refreshStepsValuesToday: distance = "+distanceString);
        }
        else{
            prefs.edit().putString(PreferencesTypesStepCounter.todaySteps.toString(), "0").apply();

            String stepsString = getString(R.string.steps)+": 0";
            stepsTodayText.setText(stepsString);
            Log.v("StepCounter", "refreshStepsValuesToday: steps = "+stepsString);

            String caloriesString = getString(R.string.calories)+": 0.00";
            caloriesTodayText.setText(caloriesString);

            String distanceString = getString(R.string.distance)+": 0.000 km";
            distanceTodayText.setText(distanceString);
        }
    }

    private void refreshStepsValuesTotal(int steps){
        if(steps > 0){
            String stepValue = String.valueOf(steps);
            prefs.edit().putString(PreferencesTypesStepCounter.totalSteps.toString(), stepValue).apply();
            String stepsString = getString(R.string.steps)+": "+stepValue;
            stepsTotalText.setText(stepsString);
            Log.v("StepCounter", "refreshStepsValuesTotal: steps = "+stepsString);

            double calories =  calculateCaloriesBySteps(steps);
            String caloriesStringValue = formatterCalories.format(calories);
            String caloriesString = getString(R.string.calories)+": "+caloriesStringValue;
            caloriesTotalText.setText(caloriesString);
            Log.v("StepCounter", "refreshStepsValuesTotal: calories = "+caloriesString);

            double distance =  calculateKmBySteps(steps);
            String distanceStringValue = formatterDistance.format(distance);
            String distanceString = getString(R.string.distance)+": "+distanceStringValue+" km";
            distanceTotalText.setText(distanceString);
            Log.v("StepCounter", "refreshStepsValuesTotal: distance = "+distanceString);
        }
        else{
            prefs.edit().putString(PreferencesTypesStepCounter.totalSteps.toString(), "0").apply();

            String stepsString = getString(R.string.steps)+": 0";
            stepsTotalText.setText(stepsString);
            Log.v("StepCounter", "refreshStepsValuesToday: steps = "+stepsString);

            String caloriesString = getString(R.string.calories)+": 0.00";
            caloriesTotalText.setText(caloriesString);

            String distanceString = getString(R.string.distance)+": 0.000 km";
            distanceTotalText.setText(distanceString);
        }

    }

    private double calculateCaloriesBySteps (int steps){
        if(steps > 0){
            return steps*0.05;
        }
        else{
            return 0.0;
        }
    }

    private double calculateKmBySteps (int steps){
        if(steps > 0){
            return steps/1312.33595801;
        }
        else{
            return 0.0;
        }
    }
}
