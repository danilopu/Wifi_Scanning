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
    int strength;
    String officeNetwork;


    public WifiReceiver(WifiManager wifiManager, ListView wifiDeviceList, int strength, String officeNetwork) {
        this.wifiManager = wifiManager;
        this.wifiDeviceList = wifiDeviceList;
        this.officeNetwork = officeNetwork;
        this.strength = strength;
    }

    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        String mainNetwork = "RationaleAP";
        String ainNetwork = intent.getExtras().getString("extra");
        if(action.equals("getStrengthLimit")){
            mainNetwork = intent.getExtras().getString("extra");
        }
        String network = intent.getStringExtra("network");
        if (WifiManager.SCAN_RESULTS_AVAILABLE_ACTION.equals(action)) {
            sb = new StringBuilder();
            List<ScanResult> wifiList = wifiManager.getScanResults();

            ArrayList<String> deviceList = new ArrayList<>();
            for (ScanResult scanResult : wifiList) {
                if(scanResult.SSID.equals(this.officeNetwork) ){
                    if(Math.abs(scanResult.level) < Math.abs(this.strength)){
                        String signalStrength = "Signal Strength: " + scanResult.level;
                        deviceList.add((scanResult.SSID + " - " + scanResult.BSSID ) + " - " + scanResult.frequency + " - " + scanResult.level + "\n - in range");
                    } else {
                        String signalStrength = "Signal Strength: " + scanResult.level;
                        deviceList.add((scanResult.SSID + " - " + scanResult.BSSID ) + " - " + scanResult.frequency + " - " + scanResult.level + "\n - out of range");
                    }
                }

            }

            ArrayAdapter arrayAdapter = new ArrayAdapter(context, android.R.layout.simple_list_item_1, deviceList.toArray());
            wifiDeviceList.setAdapter(arrayAdapter);
        }
    }


}
