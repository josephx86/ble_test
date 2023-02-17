package com.example.delblechat.model;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothDeviceReceiver extends BroadcastReceiver {
    private final ScanResultsHandler handler;

    public BluetoothDeviceReceiver(ScanResultsHandler handler) {
        this.handler = handler;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
            BluetoothDevice device =
                    (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU)
                            ? intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE, BluetoothDevice.class) :
                            intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            if (device != null) {
                addDevice(device);
            }
        }
    }

    public void addDevice(BluetoothDevice device) {
        handler.checkDevice(device);
    }
}
