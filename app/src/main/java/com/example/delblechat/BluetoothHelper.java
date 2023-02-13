package com.example.delblechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;

public class BluetoothHelper {
    private BluetoothAdapter adapter;

    public BluetoothHelper() {
        BluetoothManager manager = (BluetoothManager) BleChat.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            adapter = manager.getAdapter();
        }
    }

    public boolean supportsMultipleAdvertisement() {
        boolean supported = false;
        if (adapter != null) {
            supported = adapter.isMultipleAdvertisementSupported();
        }
        return supported;
    }

    public boolean isBluetoothOn() {
        boolean isOn = false;
        if (adapter != null) {
            isOn = adapter.isEnabled();
        }
        return isOn;
    }

    public Intent getEnablingIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }
}
