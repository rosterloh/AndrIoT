package com.rosterloh.andriot.dash;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.andriot.databinding.DashFragBinding;
import com.rosterloh.andriot.ui.SnackbarChangedCallback;

public class DashFragment extends Fragment {

    private DashViewModel dashViewModel;

    private SnackbarChangedCallback snackBarChangedCallback;

    private DashFragBinding dashFragBinding;

    public DashFragment() {
        // Requires empty public constructor
    }

    public static DashFragment newInstance() {
        return new DashFragment();
    }

    @Override
    public void onResume() {
        super.onResume();
        dashViewModel.onViewResumed();
    }

    @Override
    public void onStop() {
        super.onStop();
        dashViewModel.onViewDetached();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        dashFragBinding = DashFragBinding.inflate(inflater, container, false);
        dashFragBinding.setView(this);
        dashFragBinding.setViewmodel(dashViewModel);

        return dashFragBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setupTextViews();
        setupSnackbar();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        dashViewModel.snackbarText.removeOnPropertyChangedCallback(snackBarChangedCallback);
    }

    void setupTextViews() {

        Typeface weatherFontIcon = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/weathericons-regular-webfont.ttf");
        Typeface robotoThin = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Thin.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(getActivity().getAssets(),
                "fonts/Roboto-Black.ttf");

        dashFragBinding.tvWeatherIcon.setTypeface(weatherFontIcon);
        dashFragBinding.tvTemperature.setTypeface(robotoThin);
        dashFragBinding.tvDescription.setTypeface(robotoThin);
        dashFragBinding.tvLastUpdate.setTypeface(robotoThin);
        dashFragBinding.tcTime.setTypeface(robotoBlack);
        dashFragBinding.tcDate.setTypeface(robotoBlack);
        dashFragBinding.tvWifiInfo.setTypeface(robotoThin);
        dashFragBinding.tvEthInfo.setTypeface(robotoThin);
    }

    private void setupSnackbar() {
        snackBarChangedCallback = new SnackbarChangedCallback(getView(), dashViewModel);
        dashViewModel.snackbarText.addOnPropertyChangedCallback(snackBarChangedCallback);
    }

    public void setViewModel(DashViewModel viewModel) {
        dashViewModel = viewModel;
    }
}
