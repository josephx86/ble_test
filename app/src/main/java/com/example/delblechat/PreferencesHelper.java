package com.example.delblechat;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

public class PreferencesHelper {
    private final SharedPreferences preferences;
    private static final String PREF_LOCATION_REQUESTED = "location_requested";
    private static final String PREF_BLUETOOTH_REQUESTED = "bluetooth_requested";

    public PreferencesHelper(Context context) {
        preferences = PreferenceManager.getDefaultSharedPreferences(context);
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
