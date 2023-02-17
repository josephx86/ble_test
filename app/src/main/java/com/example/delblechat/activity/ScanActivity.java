package com.example.delblechat.activity;

import android.bluetooth.BluetoothDevice;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.delblechat.R;
import com.example.delblechat.databinding.ActivityScanBinding;
import com.example.delblechat.helper.BluetoothHelper;
import com.example.delblechat.model.BluetoothDeviceReceiver;
import com.example.delblechat.model.DeviceInfo;
import com.example.delblechat.model.DeviceInfoAdapter;
import com.example.delblechat.model.ScanResultsHandler;
import com.google.android.material.snackbar.Snackbar;

public class ScanActivity extends AppCompatActivity {
    private ActivityScanBinding binding;
    private final BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance();
    private final ScanResultsHandler scanResultsHandler = new ScanResultsHandler(this::onDeviceFound);
    private final DeviceInfoAdapter adapter = new DeviceInfoAdapter();
    private final BluetoothDeviceReceiver deviceReceiver = new BluetoothDeviceReceiver(scanResultsHandler);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        setupDeviceRecyclerView();
        registerReceiver(deviceReceiver, new IntentFilter(BluetoothDevice.ACTION_FOUND));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(deviceReceiver);
    }

    private void setupDeviceRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.devicesRecyclerView.setLayoutManager(layoutManager);
        binding.devicesRecyclerView.setAdapter(adapter);
    }

    private void setListeners() {
        binding.scanButton.setOnClickListener(v -> {
            boolean alreadyScanning = bluetoothHelper.isScanning();
            if (!alreadyScanning) {
                adapter.clear();
            }
            boolean scanLe = binding.bluetoothLeRadioButton.isChecked();
            String error = bluetoothHelper.startScan(scanLe, this::scanUpdate, scanResultsHandler);
            if (!TextUtils.isEmpty(error)) {
                Snackbar.make(binding.coordinator, error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void scanUpdate(final int remaining) {
        runOnUiThread(() -> {
            String label = getString(R.string.scan);
            if (remaining > 0) {
                label = getString(R.string.stop, remaining);
            }
            binding.scanButton.setText(label);
        });
    }

    private void onDeviceFound(DeviceInfo info) {
        adapter.add(info);
    }
}