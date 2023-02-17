package com.example.delblechat.model;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.text.TextUtils;

import androidx.core.app.ActivityCompat;

import com.example.delblechat.BleChat;
import com.example.delblechat.callback.DeviceInfoCallback;

public class ScanResultsHandler extends ScanCallback {
    private final DeviceInfoCallback callback;

    public ScanResultsHandler(DeviceInfoCallback callback) {
        this.callback = callback;
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {
        super.onScanResult(callbackType, result);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (result.isConnectable()) {
                checkDevice(result.getDevice());
            }
        } else {
            checkDevice(result.getDevice());
        }
    }

    public void checkDevice(BluetoothDevice device) {
        if (device == null) {
            return;
        }
        if (ActivityCompat.checkSelfPermission(BleChat.getInstance(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return;
            }
        }
        String name = device.getName();
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
            String alias = device.getAlias();
            if (!TextUtils.isEmpty(alias)) {
                name += " (" + alias + ")";
            }
        }
        String address = device.getAddress();
        DeviceInfo info = new DeviceInfo(name, address);
        if (callback != null) {
            callback.handler(info);
        }
    }
}
