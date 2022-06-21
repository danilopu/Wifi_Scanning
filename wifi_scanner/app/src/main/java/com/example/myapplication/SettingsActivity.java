package com.example.myapplication;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class SettingsActivity extends AppCompatActivity {

    private EditText officeNetworkName;
    private EditText signalStrength;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LoadSettings();
    }

    public void saveSettings(View view){
        SharedPreferences sharedPreferences = getSharedPreferences("settings",MODE_PRIVATE);
        SharedPreferences.Editor settingsEditor = sharedPreferences.edit();
        officeNetworkName = findViewById(R.id.officeNetworkName);
        signalStrength = findViewById(R.id.strengthLevel);

        settingsEditor.putString("networkName", officeNetworkName.getText().toString());
        settingsEditor.putString("dBmLevel", signalStrength.getText().toString());
        settingsEditor.commit();
    }

    public void LoadSettings() {
        Intent intent = getIntent();
        String office = intent.getStringExtra("name");
        String strength = intent.getStringExtra("strength");

        officeNetworkName = findViewById(R.id.officeNetworkName);
        signalStrength = findViewById(R.id.strengthLevel);

        officeNetworkName.setText(office);
        signalStrength.setText(strength);
    }
}