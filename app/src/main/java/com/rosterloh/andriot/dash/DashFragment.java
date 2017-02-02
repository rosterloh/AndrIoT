package com.rosterloh.andriot.dash;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.andriot.data.ConnectionDetector;
import com.rosterloh.andriot.databinding.DashFragBinding;
import com.rosterloh.andriot.weather.Weather;

import java.net.InetAddress;
import java.util.Map;

public class DashFragment extends Fragment implements DashContract.View {

    private static final String TAG = "Dashboard";

    private DashContract.Presenter presenter;

    private DashViewModel dashViewModel;

    public DashFragment() {
        // Requires empty public constructor
    }

    public static DashFragment newInstance() {
        return new DashFragment();
    }

    @Override
    public void setPresenter(@NonNull DashContract.Presenter presenter) {
        this.presenter = presenter;
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onViewResumed();
    }

    @Override
    public void onStop() {
        super.onStop();
        presenter.onViewDetached();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        presenter.result(requestCode, resultCode);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        DashFragBinding dashFragBinding = DashFragBinding.inflate(inflater, container, false);

        dashFragBinding.setDash(dashViewModel);

        setupTextViews(dashFragBinding);

        return dashFragBinding.getRoot();
    }

    void setupTextViews(DashFragBinding binding) {

        Typeface weatherFontIcon = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/weathericons-regular-webfont.ttf");
        Typeface robotoThin = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Thin.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Black.ttf");

        binding.tvWeatherIcon.setTypeface(weatherFontIcon);
        binding.tvTemperature.setTypeface(robotoThin);
        binding.tvDescription.setTypeface(robotoThin);
        binding.tvLastUpdate.setTypeface(robotoThin);
        binding.tcTime.setTypeface(robotoBlack);
        binding.tcDate.setTypeface(robotoBlack);
        binding.tvWifiInfo.setTypeface(robotoThin);
        binding.tvEthInfo.setTypeface(robotoThin);
    }

    public void setViewModel(DashViewModel viewModel) {
        dashViewModel = viewModel;
    }

    @Override
    public void setLoadingIndicator(final boolean active) {

        if (getView() == null) {
            return;
        }
        dashViewModel.setLoading(active);
    }

    public void showWeather(Weather weather) {
        dashViewModel.setWeather(weather);
    }

    @Override
    public void setNetworkInfo(ConnectionDetector network) {

        Map<String, InetAddress> ips = network.getIpAddresses();
        if(ips.containsKey("eth0"))
            dashViewModel.setEthIp(ips.get("eth0"));

        if(ips.containsKey("wlan0")) {
            dashViewModel.setWifiIp(ips.get("wlan0"));
            dashViewModel.setWifiName(network.getWifiSSid());
        }
    }

    @Override
    public void showSuccessMessage() {
        Log.i(TAG, "Success");
    }

    @Override
    public void showLoadingError() {
        Log.e(TAG, "Error loading data");
    }

    @Override
    public boolean isActive() {
        return isAdded();
    }
}
