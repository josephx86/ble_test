package com.example.delblechat.helper;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
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
    private ScheduledFuture<?> executorHandle;

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
        return isScanningClassic() || (executorHandle != null);
    }

    public boolean isScanningClassic() {
        boolean scanningClassic = false;
        BluetoothAdapter adapter = getAdapter();
        if (adapter != null) {
            if (ActivityCompat.checkSelfPermission(BleChat.getInstance(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
                scanningClassic = adapter.isDiscovering();
            }
        }
        return scanningClassic;
    }

    public Intent getEnablingIntent() {
        return new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
    }

    public IntentFilter getBroadcastFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    private String stopScan(ScanResultsHandler handler, IntCallback callback) {
        if (ContextCompat.checkSelfPermission(BleChat.getInstance(), Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                return "Set bluetooth permissions in app settings!";
            }
        }
        if (isScanningClassic()) {
            BluetoothAdapter adapter = getAdapter();
            if (adapter != null) {
                adapter.cancelDiscovery();
            }
        }
        if (executorHandle != null) {
            BluetoothLeScanner scanner = getScanner();
            if (scanner != null) {
                scanner.stopScan(handler);
            }
        }
        stopExecutor();
        if (callback != null) {
            callback.handler(-1);
        }
        return null;
    }

    public String startScan(boolean scanningLe, IntCallback callback, ScanResultsHandler handler) {
        if (isScanning()) {
            return stopScan(handler, callback);
        } else if (scanningLe) {
            return startLeScan(callback, handler);
        } else {
            return startClassicScan(callback, handler);
        }
    }

    private String startClassicScan(IntCallback callback, ScanResultsHandler handler) {
        BluetoothAdapter adapter = getAdapter();
        final Context context = BleChat.getInstance().getApplicationContext();
        if (adapter != null) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return "Set bluetooth permissions in app settings!";
                }
            }
            scannerElapsed = 0;
            executorHandle = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                if (scannerElapsed >= SCAN_DURATION) {
                    stopScan(handler, callback);
                    return;
                }
                scannerElapsed++;
                if (callback != null) {
                    callback.handler(SCAN_DURATION - scannerElapsed);
                }
            }, 0, 1, TimeUnit.SECONDS);

            for (BluetoothDevice device: adapter.getBondedDevices()) {
                handler.checkDevice(device);
            }

            adapter.startDiscovery();
        }
        return null;
    }

    private String startLeScan(IntCallback callback, ScanResultsHandler handler) {
        BluetoothLeScanner scanner = getScanner();
        final Context context = BleChat.getInstance().getApplicationContext();
        if (scanner != null) {
            if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    return "Set bluetooth permissions in app settings!";
                }
            }
            scannerElapsed = 0;
            executorHandle = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
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
        if (executorHandle != null) {
            executorHandle.cancel(true);
        }
        executorHandle = null;
    }
}
