package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ContentResolver;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.rosterloh.andriot.R;
import com.rosterloh.andriot.databinding.DashFragmentBinding;
import com.rosterloh.andriot.nearby.ConnectionsServer;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;
import timber.log.Timber;

public class DashFragment extends LifecycleFragment {

    @Inject
    ViewModelProvider.Factory mViewModelFactory;

    @Inject
    ConnectionsServer mConnectionsServer;

    @Inject
    ContentResolver mContentResolver;

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

        try {
            int mode = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE);
            if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                Timber.d("Setting SCREEN_BRIGHTNESS_MODE to MANUAL");
                Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS_MODE,
                        Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
            }
        } catch (Settings.SettingNotFoundException e) {
            Timber.e("Could find SCREEN_BRIGHTNESS_MODE setting");
        }

        mDashViewModel.getMotion().observe(this, value -> {
            mBinding.setMotion(value);
            if (value != null) {
                try {
                    if (value)
                        Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, 255);
                    else
                        Settings.System.putInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS, 1);

                    int brightness = Settings.System.getInt(mContentResolver, Settings.System.SCREEN_BRIGHTNESS);
                    Timber.d("Screen brightness set to " + brightness);
                    WindowManager.LayoutParams lp = getActivity().getWindow().getAttributes();
                    lp.screenBrightness = (float) brightness / 255;
                    getActivity().getWindow().setAttributes(lp);
                } catch (Settings.SettingNotFoundException e) {
                    Timber.w("Could not set SCREEN_BRIGHTNESS");
                }
            } else {
                Timber.w("NULL");
            }
        });
        mDashViewModel.getSensorData().observe(this, sensors -> {
            if (sensors.size() > 0) {
                mBinding.setSensors(sensors.get(sensors.size()));
            }
        });
        mDashViewModel.getWeather().observe(this, weather -> mBinding.setWeather(weather));
    }
}
