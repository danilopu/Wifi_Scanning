package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    private ListView wifiList;
    private WifiManager wifiManager;

    private EditText networkName;
    private TextView mainWindowInfo;
    boolean isWifiConnected;

    private TextView countDownTimer;
    String newNetwork;
    private EditText dbmLevel;
    String dBm;

    long hourPassed = 0;
    long minutePassed = 0;
    long secondsPassed = 0;

    int hourPassedUp = 0;
    int minutePassedUp = 0;
    int secondsPassedUp = 0;

    int hours = 0;
    int minutes = 0;
    int seconds = 0;

    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    WifiReceiver receiverWifi;
    private Executor executor;

    TextView timerUpText;
    Timer timer;
    TimerTask timerTask;
    double time = 0.0;

    boolean timerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        timerUpText = findViewById(R.id.countUpTimer);
        networkName = findViewById(R.id.networkName);

        timer = new Timer();
        wifiList = findViewById(R.id.wifiList);
        Button buttonScan = findViewById(R.id.scanBtn);

        dbmLevel = findViewById(R.id.dbmLevel);

        InitSettingUp(sharedPreferences);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SaveSettingsToLocal();
                if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(
                            MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION
                    );
                } else {
                    wifiManager.startScan();
                }
            }
        });
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        receiverWifi = new WifiReceiver(wifiManager, wifiList);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        registerReceiver(receiverWifi, intentFilter);
        getWifi();
    }

    private void getWifi() {
        String workNetwork = networkName.getText().toString();
        isWifiConnected = CheckWifiConnection();
        if(isWifiConnected) {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String connectedNetwork = wifiInfo.getSSID();
            String formatConnectedNetwork = connectedNetwork.substring(1, connectedNetwork.length() - 1);
            if(formatConnectedNetwork.equals(workNetwork)){
                Toast toast = Toast.makeText(MainActivity.this, "Connected to work network: " + wifiInfo.getSSID(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0,0);
                toast.show();

                Toast toastTimer = Toast.makeText(MainActivity.this, "Started logging time!", Toast.LENGTH_LONG);
                toastTimer.setGravity(Gravity.CENTER, 0,0);
                toastTimer.show();
                startTimer();

            } else {

                Toast toast = Toast.makeText(MainActivity.this, "Not connected to work network: " + networkName.getText().toString(), Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0,0);
                toast.show();

            }
        } else {
            timerTask.cancel();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast toast = Toast.makeText(MainActivity.this, "version>=marshmallow", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
            } else {
                Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
                wifiManager.startScan();
            }
        } else {
            Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
            wifiManager.startScan();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        SaveSettingsToLocal();
        unregisterReceiver(receiverWifi);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case MY_PERMISSIONS_ACCESS_COARSE_LOCATION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(MainActivity.this, "permission granted", Toast.LENGTH_SHORT).show();
                    wifiManager.startScan();
                } else {

                    Toast.makeText(MainActivity.this, "permission not granted", Toast.LENGTH_SHORT).show();
                    return;
                }
                break;
        }
    }

    public boolean CheckWifiConnection() {
        ConnectivityManager conMgr = (ConnectivityManager) getSystemService (Context.CONNECTIVITY_SERVICE);
        if (conMgr.getActiveNetworkInfo() != null
                && conMgr.getActiveNetworkInfo().isAvailable()
                && conMgr.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            return false;
        }
    }

    private void startTimer()
    {
        timerTask = new TimerTask()
        {
            @Override
            public void run()
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        time++;
                        timerUpText.setText(getTimerText());
                        wifiManager.startScan();
                    }
                });
            }

        };
        timer.scheduleAtFixedRate(timerTask, 0 ,1000);
    }

    private String getTimerText()
    {
        int rounded = (int) Math.round(time);

        secondsPassedUp = ((rounded % 86400) % 3600) % 60;
        minutePassedUp = ((rounded % 86400) % 3600) / 60;
        hourPassedUp = ((rounded % 86400) / 3600);

        return formatTime(secondsPassedUp, minutePassedUp, hourPassedUp);
    }

    private String formatTime(int seconds, int minutes, int hours)
    {
        return String.format("%02d",hours) + " : " + String.format("%02d",minutes) + " : " + String.format("%02d",seconds);
    }

    public void startStopTapped(View view)
    {
        if(timerStarted == false)
        {
            timerStarted = true;
            startTimer();
        }
        else
        {
            timerStarted = false;
            timerTask.cancel();
        }
    }

    public void StopClock(){
        timerTask.cancel();
    }

    private void InitSettingUp(SharedPreferences sharedPreferences){
        String network = sharedPreferences.getString("networkName", "");
        String dBmLevel = sharedPreferences.getString("dBmLevel", "");
//        Toast.makeText(MainActivity.this, "Home network:  " + network, Toast.LENGTH_LONG).show();
        if(!network.isEmpty())
            networkName.setText(network);
        String hoursLeftUp = sharedPreferences.getString("hoursPassedUp", "");
        String minutesLeftUp = sharedPreferences.getString("minutesPassedUp", "");
        String secondsLeftUp = sharedPreferences.getString("secondsPassedUp", "");
//        Toast.makeText(MainActivity.this, "Hours passed " + hoursLeftUp, Toast.LENGTH_LONG).show();
//        Toast.makeText(MainActivity.this, "Minutes passed " + minutesLeftUp, Toast.LENGTH_LONG).show();
//        Toast.makeText(MainActivity.this, "Seconds passed " + secondsLeftUp, Toast.LENGTH_LONG).show();

        try{
            int dbmlevel = Integer.parseInt(dBmLevel);
            dbmLevel.setText(String.valueOf(dbmlevel));
        } catch(NumberFormatException ex) {

        }
        try{
            String loggedTime = (sharedPreferences.getString("timeDouble", ""));
            time = Double.parseDouble(loggedTime);
        } catch(NumberFormatException ex) {

        }
        try{
            seconds = Integer.parseInt(secondsLeftUp);
        } catch(NumberFormatException ex){

        }
        try{
            minutes = Integer.parseInt(minutesLeftUp);
        } catch(NumberFormatException ex){

        }
        try{
            hours = Integer.parseInt(hoursLeftUp);
        } catch(NumberFormatException ex){

        }

        timerUpText.setText(formatTime( seconds, minutes ,hours ));
    }

    private double setTimeVariable(int seconds) {
        double time = ((seconds * 60) * 3600) * seconds;

        return time;
    }

    private void SaveSettingsToLocal(){
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();

        settingsEditor.putString("hoursPassedUp", String.valueOf(hourPassedUp));
        settingsEditor.putString("minutesPassedUp", String.valueOf(minutePassedUp));
        settingsEditor.putString("secondsPassedUp", String.valueOf(secondsPassedUp));
        settingsEditor.putString("timeDouble", String.valueOf(time));
        settingsEditor.putString("networkName", networkName.getText().toString());
        settingsEditor.putString("dBmLevel", dbmLevel.getText().toString());
        settingsEditor.commit();
    }

    public void ResetSettingsLocal(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();

        settingsEditor.putString("hoursPassedUp", "");
        settingsEditor.putString("minutesPassedUp", "");
        settingsEditor.putString("secondsPassedUp", "");
        settingsEditor.putString("timeDouble", "");
        settingsEditor.commit();
    }
}
