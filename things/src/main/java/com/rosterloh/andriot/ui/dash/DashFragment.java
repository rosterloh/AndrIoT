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
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ConnectionsServer mConnectionsServer;

    private DashFragmentBinding mBinding;
    private DashViewModel mDashViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.dash_fragment, container, false);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);
        mDashViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DashViewModel.class);

        mDashViewModel.getMotion().observe(this, value -> mBinding.setMotion(value));
        mDashViewModel.getSensorData().observe(this, sensors -> mBinding.setSensors(sensors));
        mDashViewModel.getWeather().observe(this, weather -> mBinding.setWeather(weather));
    }
}
