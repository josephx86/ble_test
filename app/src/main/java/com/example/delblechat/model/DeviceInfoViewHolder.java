package com.example.delblechat.model;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.delblechat.databinding.LayoutDeviceInfoBinding;

public class DeviceInfoViewHolder extends RecyclerView.ViewHolder {
    private final LayoutDeviceInfoBinding binding;

    public DeviceInfoViewHolder(@NonNull View itemView) {
        super(itemView);
        binding = LayoutDeviceInfoBinding.bind(itemView);
    }

    public void setInfo(DeviceInfo info) {
        binding.nameTextView.setText(info.getName());
        binding.addressTextView.setText(info.getAddress());
    }
}
