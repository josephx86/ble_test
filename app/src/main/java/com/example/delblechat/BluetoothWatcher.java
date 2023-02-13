package com.example.delblechat;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BluetoothWatcher extends BroadcastReceiver {
    private final VoidCallback callback;

    public BluetoothWatcher(VoidCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
            if (callback != null) {
                callback.handler();
            }
        }
    }
}
