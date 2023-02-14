package com.example.delblechat.model;

import android.text.TextUtils;

import java.util.Objects;

public class DeviceInfo {
    private final String name;
    private final String address;

    public DeviceInfo(String name, String address) {
        this.name = name;
        this.address = address;
    }

    public String getAddress() {
        String deviceAddress = address;
        if (deviceAddress == null) {
            deviceAddress = "";
        }
        deviceAddress = deviceAddress.trim();
        if (TextUtils.isEmpty(deviceAddress)) {
            deviceAddress = "<no address>";
        }
        return deviceAddress;
    }

    public String getName() {
        String deviceName = name;
        if (deviceName == null) {
            deviceName = "";
        }
        deviceName = deviceName.trim();
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = "<no name>";
        }
        return deviceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeviceInfo that = (DeviceInfo) o;

        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return address != null ? address.hashCode() : 0;
    }
}
