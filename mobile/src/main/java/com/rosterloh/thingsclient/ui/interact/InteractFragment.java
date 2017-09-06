package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.databinding.InteractFragmentBinding;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class InteractFragment extends LifecycleFragment {

    @Inject
    InteractViewModelFactory mViewModelFactory;

    InteractFragmentBinding mBinding;
    InteractViewModel mViewModel;

    private GoogleMap mGoogleMap;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.interact_fragment,container, false);
        mBinding.setCallback(() -> mViewModel.setLocation());
        mBinding.mapView.onCreate(savedInstanceState);
        mBinding.mapView.getMapAsync(map -> mGoogleMap = map);
        return mBinding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory)
                .get(InteractViewModel.class);

        mViewModel.getStatus().observe(this, status -> mBinding.setStatus(status));
        mViewModel.getLocation().observe(this, location -> {
            Double lat = location.getLatitude();
            Double lon = location.getLongitude();

            if(mGoogleMap != null) {
                mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lon), 15.0f));

                mGoogleMap.addMarker(new MarkerOptions()
                        .position(new LatLng(lat, lon))
                        .title("AndrIoT")
                        .icon(BitmapDescriptorFactory.defaultMarker()));
            }
        });
    }

    @Override
    public void onResume() {
        mBinding.mapView.onResume();
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mBinding.mapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mBinding.mapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mBinding.mapView.onLowMemory();
    }
}
