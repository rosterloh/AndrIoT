package com.rosterloh.thingsclient.ui.scan;

import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.rosterloh.things.common.ui.DataBoundListAdapter;
import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.databinding.ScanItemBinding;
import com.rosterloh.thingsclient.vo.ScannedDevice;

import java.util.Locale;
import java.util.Objects;

/**
 * A RecyclerView adapter for {@link ScannedDevice} class.
 */
public class ScannedDeviceAdapter extends DataBoundListAdapter<ScannedDevice, ScanItemBinding> {
    private final static int RSSI_BAR_LEVELS = 5;
    private final static int RSSI_BAR_SCALE = 100 / RSSI_BAR_LEVELS;

    private final DataBindingComponent dataBindingComponent;
    private final DeviceClickCallback deviceClickCallback;

    public ScannedDeviceAdapter(DataBindingComponent dataBindingComponent,
                                DeviceClickCallback deviceClickCallback) {
        this.dataBindingComponent = dataBindingComponent;
        this.deviceClickCallback = deviceClickCallback;
    }

    @Override
    protected ScanItemBinding createBinding(ViewGroup parent) {
        ScanItemBinding binding = DataBindingUtil
                .inflate(LayoutInflater.from(parent.getContext()), R.layout.scan_item,
                        parent, false, dataBindingComponent);
        binding.getRoot().setOnClickListener(v -> {
            ScannedDevice device = binding.getDevice();
            if (device != null && deviceClickCallback != null) {
                deviceClickCallback.onClick(device);
            }
        });
        return binding;
    }

    @Override
    protected void bind(ScanItemBinding binding, ScannedDevice item) {
        binding.setDevice(item);
        binding.setLevel(Math.min(RSSI_BAR_LEVELS - 1, (127 + item.rssi + 5) / RSSI_BAR_SCALE));
    }

    @Override
    protected boolean areItemsTheSame(ScannedDevice oldItem, ScannedDevice newItem) {
        return Objects.equals(oldItem.btDevice, newItem.btDevice);
    }

    @Override
    protected boolean areContentsTheSame(ScannedDevice oldItem, ScannedDevice newItem) {
        return Objects.equals(oldItem.rssi, newItem.rssi);
    }

    public interface DeviceClickCallback {
        void onClick(ScannedDevice device);
    }
}
