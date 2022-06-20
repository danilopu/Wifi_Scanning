package com.example.myapplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

class WifiReceiver extends BroadcastReceiver {

    WifiManager wifiManager;
    StringBuilder sb;
    ListView wifiDeviceList;


    public WifiReceiver(WifiManager wifiManager, ListView wifiDeviceList) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
    }

    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        String action = intent.getAction();
        String mainNetwork = "RationaleAP";
        String network = intent.getStringExtra("network");
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();

            ArrayList<String> deviceList = new ArrayList<>();
            for (ScanResult scanResult : wifiList) {
                if(scanResult.SSID.equals(mainNetwork) ){
                    String signalStrength = "Signal Strength: " + scanResult.level;
                    int strength = wifiManager.calculateSignalLevel(scanResult.level, 10);
                    deviceList.add((scanResult.SSID ) + " - " +signalStrength + "\n" + "Strength Level: " + strength + " - Frequency: " + scanResult.frequency);
                }

            }
            Toast.makeText(context, sb, Toast.LENGTH_SHORT).show();

            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());

            wifiDeviceList.setAdapter(arrayAdapter);
        }
    }
}
