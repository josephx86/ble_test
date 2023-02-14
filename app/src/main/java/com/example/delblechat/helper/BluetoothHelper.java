package com.example.delblechat.helper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.content.ContextCompat;

import com.example.delblechat.BleChat;
import com.example.delblechat.callback.IntCallback;
import com.example.delblechat.model.ScanResultsHandler;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class BluetoothHelper {

    private static BluetoothHelper instance;
    private final BluetoothManager bluetoothManager;
    private final int SCAN_DURATION = 30; // Seconds
    private int scannerElapsed = 0;
    private ScheduledFuture<?> scannerExecutorHandle;

    public static BluetoothHelper getInstance() {
        if (instance == null) {
            instance = new BluetoothHelper();
        }
        return instance;
    }

    private BluetoothHelper() {
        bluetoothManager = (BluetoothManager) BleChat.getInstance().getSystemService(Context.BLUETOOTH_SERVICE);
    }

    @Override
    protected void finalize() {
        stopExecutor();
    }

    private BluetoothAdapter getAdapter() {
        BluetoothAdapter adapter = null;
        if (bluetoothManager != null) {
            adapter = bluetoothManager.getAdapter();
        }
        return adapter;
    }

    private BluetoothLeScanner getScanner() {
        BluetoothLeScanner scanner = null;
        BluetoothAdapter adapter = getAdapter();
        if ((adapter != null) && adapter.isEnabled()) {
            // getBluetoothLeScanner() will return null if Bluetooth is off!
            scanner = adapter.getBluetoothLeScanner();
        }
        return scanner;
    }

    public boolean supportsBle() {
        return getScanner() != null;
    }

    public boolean isBluetoothOn() {
        boolean isOn = false;
        BluetoothAdapter adapter = getAdapter();
        if (adapter != null) {
            isOn = adapter.isEnabled();
        }
        return isOn;
    }

    public boolean isScanning() {
        return scannerExecutorHandle != null;
    }

    public Intent getEnablingIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public IntentFilter getBroadcastFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    public String startLeScan(Context context, IntCallback callback, ScanResultsHandler handler) {
        BluetoothLeScanner scanner = getScanner();

        if (scanner != null) {
            if (scannerExecutorHandle != null) {
                stopExecutor();
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        return null;
                    }
                }
                scanner.stopScan(handler);
                if (callback != null) {
                    callback.handler(-1);
                }
                return null;
            }
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return "Set bluetooth permissions in app settings!";
                }
            }
            scannerElapsed = 0;
            scannerExecutorHandle = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (scannerElapsed >= SCAN_DURATION) {
                    scanner.stopScan(handler);
                    stopExecutor();
                }
                scannerElapsed++;
                if (callback != null) {
                    callback.handler(SCAN_DURATION - scannerElapsed);
                }
            }, 0, 1, TimeUnit.SECONDS);
            scanner.startScan(handler);
        }
        return null;
    }

    private void stopExecutor() {
        if (scannerExecutorHandle != null) {
            scannerExecutorHandle.cancel(true);
        }
        scannerExecutorHandle = null;
    }
}
