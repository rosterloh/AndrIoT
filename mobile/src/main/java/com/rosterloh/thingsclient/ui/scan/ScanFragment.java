package com.rosterloh.thingsclient.ui.scan;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
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
import com.rosterloh.thingsclient.databinding.ScanFragmentBinding;
import com.rosterloh.thingsclient.ui.common.NavigationController;

import javax.inject.Inject;

public class ScanFragment extends LifecycleFragment implements Injectable {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    AutoClearedValue<ScanFragmentBinding> binding;

    AutoClearedValue<ScannedDeviceAdapter> adapter;

    private ScanViewModel scanViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        ScanFragmentBinding dataBinding = DataBindingUtil
                .inflate(inflater, R.layout.scan_fragment, container, false,
                        dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        scanViewModel = ViewModelProviders.of(this, viewModelFactory).get(ScanViewModel.class);
        initRecyclerView();
        ScannedDeviceAdapter sdAdapter = new ScannedDeviceAdapter(dataBindingComponent,
                device -> navigationController.navigateToInteract(device.btDevice));
        binding.get().scanList.setAdapter(sdAdapter);
        adapter = new AutoClearedValue<>(this, sdAdapter);

        //binding.get().setCallback(() -> searchViewModel.refresh());
    }

    private void initRecyclerView() {

    }

}
