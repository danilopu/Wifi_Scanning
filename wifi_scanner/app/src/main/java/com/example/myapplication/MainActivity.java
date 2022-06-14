package com.example.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    ExecutorService executorService = Executors.newFixedThreadPool(4);

    private ListView wifiList;
    private WifiManager wifiManager;

    private EditText networkName;
    String network;
    private EditText dbmLevel;
    String dBm;

    private final int MY_PERMISSIONS_ACCESS_COARSE_LOCATION = 1;

    WifiReceiver receiverWifi;
    private Executor executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wifiList = findViewById(R.id.wifiList);
        Button buttonScan = findViewById(R.id.scanBtn);

        networkName = findViewById(R.id.networkName);
        network = networkName.getText().toString();

        dbmLevel = findViewById(R.id.dbmLevel);
        dBm = dbmLevel.getText().toString();

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            Toast.makeText(getApplicationContext(), "Turning WiFi ON...", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }
        buttonScan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
//        Intent intent = new Intent(getApplicationContext(), receiverWifi.getClass());
//        intent.putExtra("network", network);
//        intent.putExtra("dBm", dBm);
        startActivity(intent);
        registerReceiver(receiverWifi, intentFilter);
        getWifi(executor);
    }

    private void getWifi(Executor executor) {
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                    Toast.makeText(MainActivity.this, "version>=marshmallow", Toast.LENGTH_SHORT).show();
//                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                        Toast.makeText(MainActivity.this, "location turned off", Toast.LENGTH_SHORT).show();
//                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, MY_PERMISSIONS_ACCESS_COARSE_LOCATION);
//                    } else {
//                        Toast.makeText(MainActivity.this, "location turned on", Toast.LENGTH_SHORT).show();
//                        wifiManager.startScan();
//                    }
//                } else {
//                    Toast.makeText(MainActivity.this, "scanning", Toast.LENGTH_SHORT).show();
//                    wifiManager.startScan();
//                }
//            }
//        });
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(MainActivity.this, "version>=marshmallow", Toast.LENGTH_SHORT).show();
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
}
