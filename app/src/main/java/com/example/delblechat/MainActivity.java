package com.example.delblechat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.delblechat.databinding.ActivityMainBinding;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private FusedLocationProviderClient fusedLocationProviderClient;
    private PreferencesHelper preferencesHelper;

    private ActivityMainBinding binding;
    private boolean paused = false;
    private final BluetoothHelper bluetoothHelper = new BluetoothHelper();

    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationAvailability(@NonNull LocationAvailability locationAvailability) {
            super.onLocationAvailability(locationAvailability);
            runChecks();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferencesHelper = BleChat.getInstance().getPreferencesHelper();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        paused = false;
        if (!bluetoothHelper.supportsMultipleAdvertisement()) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.unsupported_device)
                    .setMessage(R.string.unsupported_device_message)
                    .setPositiveButton(android.R.string.ok, (dialog, i) -> {
                        dialog.dismiss();
                        MainActivity.this.finish();
                    })
                    .show();
        } else {
            enableLocationUpdates();
            runChecks();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        paused = true;
        if (fusedLocationProviderClient != null) {
            fusedLocationProviderClient.removeLocationUpdates(locationCallback);
            fusedLocationProviderClient = null;
        }
    }

    private void setListeners() {
        binding.bluetoothButton.setOnClickListener(v -> openSettings());
        binding.locationButton.setOnClickListener(v -> openSettings());
        binding.locationStateButton.setOnClickListener(v -> openLocationSettings());
        binding.bluetoothStateButton.setOnClickListener(v -> startActivity(bluetoothHelper.getEnablingIntent()));
    }

    private void runChecks() {
        if (paused) {
            return;
        }
        hideActions();
        if (getNeedsLocationPermission()) {
            askLocationPermission();
        } else if (mustTurnOnLocationService()) {
            showActions(false, true, needsBluetoothPermissions(), mustTurnOnBluetooth());
        } else if (needsBluetoothPermissions()) {
            askBluetoothPermissions();
        } else if (mustTurnOnBluetooth()) {
            showActions(false, false, false, true);
        } else {
            openScanner();
        }
    }

    private void openScanner() {
        Intent intent = new Intent(this, ScanActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void enableLocationUpdates() {
        if (fusedLocationProviderClient != null) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationProviderClient.requestLocationUpdates(
                new LocationRequest.Builder(400)
                        .setMaxUpdates(1000000)
                        .setMaxUpdateDelayMillis(100)
                        .setMinUpdateIntervalMillis(400)
                        .build(),
                locationCallback,
                Looper.getMainLooper()
        );
    }

    private void askLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
                String appName = getString(R.string.app_name);
                String message = getString(R.string.location_service_rationale, appName);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_required)
                        .setMessage(message)
                        .setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                            dialog.dismiss();
                            showActions(mustTurnOnLocationService(), mustTurnOnLocationService(), needsBluetoothPermissions(), mustTurnOnBluetooth());
                        })
                        .setPositiveButton(R.string.grant_permission, (dialog, i) -> {
                            dialog.dismiss();
                            doRequestLocationPermission();
                        })
                        .setCancelable(false).show();
            } else {
                boolean alreadyRequested = preferencesHelper.getLocationRequested();
                if (alreadyRequested) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.user_action_required)
                            .setMessage(R.string.manual_location)
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                                dialog.dismiss();
                                showActions(mustTurnOnLocationService(), mustTurnOnLocationService(), needsBluetoothPermissions(), mustTurnOnBluetooth());
                            })
                            .setPositiveButton(R.string.open_settings, ((dialog, which) -> {
                                dialog.dismiss();
                                openSettings();
                            }))
                            .show();
                } else {
                    doRequestLocationPermission();
                }
            }
        }
    }

    private void askBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            boolean showRationale = false;
            for (String permission : getBluetoothPermissions()) {
                if (shouldShowRequestPermissionRationale(permission)) {
                    showRationale = true;
                    break;
                }
            }
            if (showRationale) {
                String appName = getString(R.string.app_name);
                String message = getString(R.string.bluetooth_rationale, appName);
                new AlertDialog.Builder(this)
                        .setTitle(R.string.permission_required)
                        .setMessage(message)
                        .setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                            dialog.dismiss();
                            showActions(mustTurnOnLocationService(), mustTurnOnLocationService(), needsBluetoothPermissions(), mustTurnOnBluetooth());
                        })
                        .setPositiveButton(R.string.grant_permission, (dialog, i) -> {
                            dialog.dismiss();
                            doRequestBluetoothPermissions();
                        })
                        .setCancelable(false)
                        .show();
            } else {
                boolean alreadyRequested = preferencesHelper.getBluetoothRequested();
                if (alreadyRequested) {
                    new AlertDialog.Builder(this)
                            .setTitle(R.string.user_action_required)
                            .setMessage(R.string.manual_bluetooth)
                            .setCancelable(false)
                            .setNegativeButton(android.R.string.cancel, (dialog, i) -> {
                                dialog.dismiss();
                                dialog.dismiss();
                                showActions(mustTurnOnLocationService(), mustTurnOnLocationService(), needsBluetoothPermissions(), mustTurnOnBluetooth());
                            })
                            .setPositiveButton(R.string.open_settings, ((dialog, which) -> {
                                dialog.dismiss();
                                openSettings();
                            }))
                            .show();
                } else {
                    doRequestBluetoothPermissions();
                }
            }
        }
    }

    private void openSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri appUri = Uri.fromParts("package", getPackageName(), null);
        settingsIntent.setData(appUri);
        startActivity(settingsIntent);
    }

    private void openLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    private void doRequestLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            preferencesHelper.setLocationRequested();
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 0);
        }
    }

    private void doRequestBluetoothPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            preferencesHelper.setBluetoothRequested();
            requestPermissions(getBluetoothPermissions(), 0);
        }
    }

    private boolean getNeedsLocationPermission() {
        int fineLocationGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        return (fineLocationGranted != PackageManager.PERMISSION_GRANTED);
    }

    private boolean mustTurnOnLocationService() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            List<String> enabledProviders = locationManager.getProviders(true);
            // Passive very inaccurate and might as well be assumed to be the same as no location for this app.
            enabledProviders.remove("passive");
            return enabledProviders.isEmpty();
        }
        return true;
    }

    private boolean mustTurnOnBluetooth() {
        return !bluetoothHelper.isBluetoothOn();
    }

    private void showActions(boolean locationPermission, boolean locationOff, boolean bluetoothPermission, boolean bluetoothOff) {
        if (!locationPermission && !locationOff && !bluetoothPermission && !bluetoothOff) {
            return;
        }
        binding.title.setVisibility(View.VISIBLE);
        binding.locationButton.setVisibility(locationPermission ? View.VISIBLE : View.GONE);
        binding.bluetoothButton.setVisibility(bluetoothPermission ? View.VISIBLE : View.GONE);
        binding.locationStateButton.setVisibility(locationOff ? View.VISIBLE : View.GONE);
        binding.bluetoothStateButton.setVisibility(bluetoothOff ? View.VISIBLE : View.GONE);
    }

    private void hideActions() {
        binding.title.setVisibility(View.INVISIBLE);
        binding.locationButton.setVisibility(View.INVISIBLE);
        binding.bluetoothButton.setVisibility(View.INVISIBLE);
        binding.locationStateButton.setVisibility(View.INVISIBLE);
        binding.bluetoothStateButton.setVisibility(View.INVISIBLE);
    }

    private boolean needsBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            for (String permission : getBluetoothPermissions()) {
                int status = ContextCompat.checkSelfPermission(this, permission);
                if (status != PackageManager.PERMISSION_GRANTED) {
                    return true;
                }
            }
        }
        return false;
    }

    private String[] getBluetoothPermissions() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            return new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                    Manifest.permission.BLUETOOTH_ADVERTISE,
            };
        }
        return new String[0];
    }
}