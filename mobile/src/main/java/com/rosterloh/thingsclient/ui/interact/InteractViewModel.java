package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import java.util.Objects;

import javax.inject.Inject;

public class InteractViewModel extends ViewModel {

    private final MutableLiveData<String> device = new MutableLiveData<>();

    @Inject
    InteractViewModel(/*DeviceRepository deviceRepository*/) {

    }

    void setDevice(String device) {
        if (Objects.equals(this.device.getValue(), device)) {
            return;
        }
        this.device.setValue(device);
    }
}
