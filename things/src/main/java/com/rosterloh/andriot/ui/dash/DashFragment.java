package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingComponent;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.andriot.R;
import com.rosterloh.andriot.databinding.DashFragmentBinding;
import com.rosterloh.andriot.ui.common.NavigationController;
import com.rosterloh.things.common.binding.FragmentDataBindingComponent;
import com.rosterloh.things.common.di.Injectable;
import com.rosterloh.things.common.util.AutoClearedValue;

import javax.inject.Inject;

public class DashFragment extends LifecycleFragment implements Injectable {

    private static final String TAG = DashFragment.class.getSimpleName();

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    NavigationController navigationController;

    DataBindingComponent dataBindingComponent = new FragmentDataBindingComponent(this);

    AutoClearedValue<DashFragmentBinding> binding;

    private DashViewModel dashViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        DashFragmentBinding dataBinding = DataBindingUtil.inflate(inflater,
                R.layout.dash_fragment, container, false, dataBindingComponent);
        binding = new AutoClearedValue<>(this, dataBinding);
        return dataBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        dashViewModel = ViewModelProviders.of(this, viewModelFactory).get(DashViewModel.class);

        dashViewModel.getMotion().observe(this, value -> binding.get().setMotion(value));
        dashViewModel.getSensorData().observe(this, sensors -> binding.get().setSensors(sensors));
        dashViewModel.getWeather().observe(this, weather -> binding.get().setWeather(weather.data));
    }
}
