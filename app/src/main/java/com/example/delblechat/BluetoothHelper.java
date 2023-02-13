package com.example.delblechat;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

public class BluetoothHelper {
    private BluetoothAdapter adapter;

    public BluetoothHelper() {
        BluetoothManager manager = (BluetoothManager) BleChat.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
        if (manager != null) {
            adapter = manager.getAdapter();
        }
    }

    public boolean supportsBle() {
        BluetoothLeAdvertiser advertiser = null;
        if (adapter != null) {
            // getBluetoothLeAdvertiser() will return null if Bluetooth is off!
            advertiser = adapter.getBluetoothLeAdvertiser();
        }
        return advertiser != null;
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

    public IntentFilter getBroadcastFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }
}
