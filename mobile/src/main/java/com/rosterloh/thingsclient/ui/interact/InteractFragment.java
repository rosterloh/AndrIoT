package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.bluetooth.BluetoothDevice;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.things.common.binding.FragmentDataBindingComponent;
import com.rosterloh.things.common.di.Injectable;
import com.rosterloh.things.common.util.AutoClearedValue;
import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.databinding.InteractFragmentBinding;
import com.rosterloh.thingsclient.ui.common.NavigationController;

import javax.inject.Inject;

public class InteractFragment extends LifecycleFragment implements Injectable {

    private static final String DEVICE_KEY = "device";

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    AutoClearedValue<InteractFragmentBinding> binding;

    private InteractViewModel interactViewModel;

    public static InteractFragment create(BluetoothDevice device) {
        InteractFragment interactFragment = new InteractFragment();
        Bundle bundle = new Bundle();
        bundle.putString(DEVICE_KEY, device.getAddress());
        interactFragment.setArguments(bundle);
        return interactFragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        InteractFragmentBinding dataBinding = DataBindingUtil.inflate(inflater,
                R.layout.interact_fragment, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        interactViewModel = ViewModelProviders.of(this, viewModelFactory).get(InteractViewModel.class);
        interactViewModel.setDevice(getArguments().getString(DEVICE_KEY));
        interactViewModel.getDevice().observe(this, deviceResource -> {
            //binding.get().setDevice(deviceResource == null ? null : deviceResource.data);
            //binding.get().setDeviceResource(deviceResource);
            // this is only necessary because espresso cannot read data binding callbacks.
            binding.get().executePendingBindings();
        });

    }
}
