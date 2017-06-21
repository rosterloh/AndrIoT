package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.andriot.R;
import com.rosterloh.andriot.databinding.DashFragmentBinding;
import com.rosterloh.andriot.nearby.ConnectionsServer;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class DashFragment extends LifecycleFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    ConnectionsServer connectionsServer;

    DashFragmentBinding binding;

    private DashViewModel dashViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.dash_fragment, container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);
        dashViewModel = ViewModelProviders.of(this, viewModelFactory).get(DashViewModel.class);

        dashViewModel.getMotion().observe(this, value -> binding.setMotion(value));
        dashViewModel.getSensorData().observe(this, sensors -> binding.setSensors(sensors));
        dashViewModel.getWeather().observe(this, weather -> binding.setWeather(weather));
    }
}
