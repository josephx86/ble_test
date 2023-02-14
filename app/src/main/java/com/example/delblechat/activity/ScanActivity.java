package com.example.delblechat.activity;

import android.os.Bundle;
import android.text.TextUtils;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.delblechat.R;
import com.example.delblechat.databinding.ActivityScanBinding;
import com.example.delblechat.helper.BluetoothHelper;
import com.example.delblechat.model.DeviceInfo;
import com.example.delblechat.model.DeviceInfoAdapter;
import com.example.delblechat.model.ScanResultsHandler;
import com.google.android.material.snackbar.Snackbar;

public class ScanActivity extends AppCompatActivity {
    private ActivityScanBinding binding;
    private final BluetoothHelper bluetoothHelper = BluetoothHelper.getInstance();
    private final ScanResultsHandler scanResultsHandler = new ScanResultsHandler(this::onDeviceFound);
    private final DeviceInfoAdapter adapter = new DeviceInfoAdapter();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityScanBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setListeners();
        setupDeviceRecyclerView();
    }

    private void setupDeviceRecyclerView() {
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        binding.devicesRecyclerView.setLayoutManager(layoutManager);
        binding.devicesRecyclerView.setAdapter(adapter);
    }

    private void setListeners() {
        binding.scanButton.setOnClickListener(v -> {
            boolean alreadyScanning = bluetoothHelper.isScanning();
            String error = bluetoothHelper.startLeScan(this, this::scanUpdate, scanResultsHandler);
            if (!TextUtils.isEmpty(error)) {
                Snackbar.make(binding.coordinator, error, Snackbar.LENGTH_LONG).show();
            } else {
                if (!alreadyScanning) {
                    adapter.clear();
                }
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