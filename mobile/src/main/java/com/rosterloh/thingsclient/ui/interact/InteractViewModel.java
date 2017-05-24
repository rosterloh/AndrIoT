package com.rosterloh.thingsclient.ui.interact;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.rosterloh.things.common.util.AbsentLiveData;
import com.rosterloh.thingsclient.vo.Thing;

import java.util.Objects;

import javax.inject.Inject;

public class InteractViewModel extends ViewModel {

    private final MutableLiveData<String> device = new MutableLiveData<>();

    private final LiveData<Thing> thing;

    @Inject
    InteractViewModel(/*DeviceRepository deviceRepository*/) {
        thing = AbsentLiveData.create();
    }

    void setDevice(String device) {
        if (Objects.equals(this.device.getValue(), device)) {
            return;
        }
        this.device.setValue(device);
    }

    LiveData<Thing> getDevice() {
        return thing;
    }
}
