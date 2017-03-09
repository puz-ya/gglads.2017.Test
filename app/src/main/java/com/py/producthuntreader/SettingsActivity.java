package com.py.producthuntreader;

import android.content.ComponentName;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    public static final String PREFERENCES_FILENAME = "py.producthunt.settings.file";

    private SharedPreferences mSharedPreferences;

    private Switch mStatusSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.settings_toolbar));
        }

        mStatusSwitch = (Switch) findViewById(R.id.statusSwitch);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mStatusSwitch.setOnCheckedChangeListener(null);
        mStatusSwitch.setChecked(NewPostService.isServiceRunning(this));
        mStatusSwitch.setOnCheckedChangeListener(this);
    }

    //Checking switch button of service off/on
    @Override
    public void onCheckedChanged(final CompoundButton compoundButton, final boolean isChecked) {

        if (isChecked) {
            setServiceOn();
        } else {
            setServiceOff();
        }
    }

    private void setServiceOff() {

        Intent accelService = new Intent("com.py.producthuntreader.NewPostService");
        accelService.setPackage("com.py.producthuntreader");   //need to set package because security risk
        boolean isStopped = stopService(accelService);
    }

    private void setServiceOn(){
        Intent intent = new Intent(this, NewPostService.class);
        ComponentName componentName = startService(intent); //componentName just for debug
    }
}
