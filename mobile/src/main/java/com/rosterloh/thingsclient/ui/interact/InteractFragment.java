package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.LifecycleFragment;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.databinding.InteractFragmentBinding;
import com.rosterloh.thingsclient.nearby.ConnectionsClient;

import javax.inject.Inject;

import dagger.android.support.AndroidSupportInjection;

public class InteractFragment extends LifecycleFragment {

    @Inject
    ViewModelProvider.Factory viewModelFactory;

    @Inject
    ConnectionsClient connectionsClient;

    InteractFragmentBinding binding;

    InteractViewModel interactViewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.interact_fragment,container, false);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        AndroidSupportInjection.inject(this);
        super.onActivityCreated(savedInstanceState);
        interactViewModel = ViewModelProviders.of(this, viewModelFactory).get(InteractViewModel.class);
    }
}
