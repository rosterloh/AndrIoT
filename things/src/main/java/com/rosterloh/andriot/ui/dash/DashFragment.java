package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

//import com.google.android.things.device.ScreenManager;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.databinding.DashFragmentBinding;
import com.rosterloh.andriot.nearby.ConnectionsServer;
import com.rosterloh.andriot.sensors.LiveDataBus;

import javax.inject.Inject;

import dagger.android.support.DaggerFragment;

public class DashFragment extends DaggerFragment {

    @Inject
    DashViewModelFactory mViewModelFactory;

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
        super.onActivityCreated(savedInstanceState);
        mDashViewModel = ViewModelProviders.of(this, mViewModelFactory).get(DashViewModel.class);

        LiveDataBus.subscribe(LiveDataBus.SUBJECT_MOTION_DATA, this, value -> {
            if (value != null) {
                boolean currentValue = (boolean) value;
                mBinding.setMotion(currentValue);
                //ScreenManager(Display.DEFAULT_DISPLAY).setBrightness(currentValue ? 255 : 1);
            }
        });
        mDashViewModel.getSensorData().observe(this, sensors -> mBinding.setSensors(sensors));
        mDashViewModel.getWeather().observe(this, weather -> mBinding.setWeather(weather));
    }
}
