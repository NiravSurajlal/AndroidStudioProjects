package com.example.tempapp;

import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.BatteryManager;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Environment;
import android.os.Environment;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Thermometer thermometer;
    private float temperature;
    private Timer timer;
    private float battTemp;

    private String state;
    private File Root;
    private File DirRt;
    private File file;
    private boolean temp_file_exists = false;

    public static final String EXTRA_MESSAGE = "com.example.tempapp.MESSAGE";

    private BroadcastReceiver br = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // TODO Auto-generated method stub
            float temp = (float) (intent.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)/10.0);
            battTemp = temp;
            thermometer.setCurrentTemp(temp);
            Toast.makeText(context, "Temp is: "+temp, Toast.LENGTH_SHORT).show();
        }
    };

    private IntentFilter inf;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        thermometer = (Thermometer) findViewById(R.id.thermometer);
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        inf = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(br, inf);

    }

    /** Called when the user taps the Send button */
    public void sendMessage(View view) {
        // Do something in response to button


        state = Environment.getExternalStorageState();

        // SD accessible?
        if(Environment.MEDIA_MOUNTED.equals(state)){
            // creates OR uses EXISTING directory
            Root = Environment.getExternalStorageDirectory();
            DirRt = new File(Root.getAbsolutePath()+"/MyAppFile");
            if(!DirRt.exists()){
                DirRt.mkdir();
            }
            // creates text file
            file = new File(DirRt, "Message.txt");
            // temporary message
            String Message = "Temperatures:";
            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(Message.getBytes());
                fos.close();
                Toast.makeText(getApplicationContext(), "Saved to SD", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "File not Found", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Cannot Export", Toast.LENGTH_SHORT).show();
            }

        } else{
            Toast.makeText(getApplicationContext(), "SD card not found", Toast.LENGTH_SHORT).show();
        }
        temp_file_exists = true;
        // internal storage
//        String Message = "Temperature";
//        String f_name = "first.txt";
//        try{
//            FileOutputStream fos = openFileOutput(f_name, MODE_PRIVATE);
//            fos.write(Message.getBytes());
//            fos.close();
//            Toast.makeText(getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
//        } catch(FileNotFoundException e){
//            e.printStackTrace();
//        } catch(IOException e){
//            e.printStackTrace();
//        }


        Intent intent = new Intent(this, DisplayTempActivity.class);
        String message = "Battery temperature is: " + Float.toString(battTemp);
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);

    }

    @Override
    protected void onResume() {
        super.onResume();
        /**change here and unreg_all func to: use below & NOT use timer.cancel() when not simulating**/
//        loadAmbientTemperature();
        getTemp();
        String temp_to_store = Float.toString(battTemp) + ",\n";
        if(temp_file_exists){
            try {
                FileOutputStream fos = new FileOutputStream(file, true);
                fos.write(temp_to_store.getBytes());
                fos.close();
                Toast.makeText(getApplicationContext(), "Saved to SD", Toast.LENGTH_SHORT).show();
            } catch (FileNotFoundException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "File not Found", Toast.LENGTH_SHORT).show();
            } catch (IOException e){
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Cannot Export", Toast.LENGTH_SHORT).show();
            }
        }
//        simulateAmbientTemperature();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterAll();
    }


    public float getTemp(){

        /** battery temp to thermometer **/
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                temperature = battTemp;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        thermometer.setCurrentTemp(temperature);
                        getSupportActionBar().setTitle(getString(R.string.app_name) + " : " + temperature);
                    }
                });
            }
        }, 0, 500);
        return battTemp;
    }

    private void simulateAmbientTemperature() {
        timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {
                temperature = Utils.randInt(-10, 35);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        thermometer.setCurrentTemp(temperature);
                        getSupportActionBar().setTitle(getString(R.string.app_name) + " : " + temperature);
                    }
                });
            }
        }, 0, 1000);
    }

    private void loadAmbientTemperature() {
        Sensor sensor = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
//        if (sensor != null) {
//            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
//        } else {
//            Toast.makeText(this, "No Ambient Temperature Sensor !", Toast.LENGTH_LONG).show();
//        }
        sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_FASTEST);
    }

    private void unregisterAll() {
        //sensorManager.unregisterListener(this);
        timer.cancel();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.values.length > 0) {
            temperature = sensorEvent.values[0];
            thermometer.setCurrentTemp(temperature);
            getSupportActionBar().setTitle(getString(R.string.app_name) + " : " + temperature);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}