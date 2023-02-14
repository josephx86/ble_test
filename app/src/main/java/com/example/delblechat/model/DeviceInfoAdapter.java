package com.example.delblechat.model;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delblechat.databinding.LayoutDeviceInfoBinding;

import java.util.ArrayList;

public class DeviceInfoAdapter extends RecyclerView.Adapter<DeviceInfoViewHolder> {
    private final ArrayList<DeviceInfo> deviceInfos = new ArrayList<>();

    @NonNull
    @Override
    public DeviceInfoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        LayoutDeviceInfoBinding binding = LayoutDeviceInfoBinding.inflate(inflater, parent, false);
        return new DeviceInfoViewHolder(binding.getRoot());
    }

    @Override
    public void onBindViewHolder(@NonNull DeviceInfoViewHolder holder, int position) {
        DeviceInfo info = deviceInfos.get(position);
        holder.setInfo(info);
    }

    @Override
    public int getItemCount() {
        return deviceInfos.size();
    }

    public void add(DeviceInfo info) {
        if (!deviceInfos.contains(info)) {
            deviceInfos.add(info);
            notifyItemInserted(deviceInfos.size() - 1);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void clear() {
        deviceInfos.clear();
        notifyDataSetChanged();
    }
}
