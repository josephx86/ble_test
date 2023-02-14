package com.example.delblechat.helper;

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.example.delblechat.BleChat;

public class PreferencesHelper {
    private final SharedPreferences preferences;
    private static final String PREF_LOCATION_REQUESTED = "location_requested";
    private static final String PREF_BLUETOOTH_REQUESTED = "bluetooth_requested";

    private static PreferencesHelper instance;

    public static PreferencesHelper getInstance() {
        if (instance == null) {
            instance = new PreferencesHelper();
        }
        return instance;
    }

    private PreferencesHelper() {
        preferences = PreferenceManager.getDefaultSharedPreferences(BleChat.getInstance());
    }

    public void setBluetoothRequested() {
        preferences.edit().putBoolean(PREF_BLUETOOTH_REQUESTED, true).apply();
    }

    public boolean getBluetoothRequested() {
        return preferences.getBoolean(PREF_BLUETOOTH_REQUESTED, false);
    }

    public void setLocationRequested() {
        preferences.edit().putBoolean(PREF_LOCATION_REQUESTED, true).apply();
    }

    public boolean getLocationRequested() {
        return preferences.getBoolean(PREF_LOCATION_REQUESTED, false);
    }
}
