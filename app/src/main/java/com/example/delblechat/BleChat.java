package com.example.delblechat;

import android.app.Application;

public class BleChat extends Application {
    private static BleChat instance;
    private PreferencesHelper preferencesHelper;

    public static BleChat getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        preferencesHelper = new PreferencesHelper(this);
    }

    public PreferencesHelper getPreferencesHelper() {
        return preferencesHelper;
    }
}
