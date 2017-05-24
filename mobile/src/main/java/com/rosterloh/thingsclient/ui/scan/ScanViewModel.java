package com.rosterloh.thingsclient.ui.scan;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.rosterloh.things.common.util.AbsentLiveData;
import com.rosterloh.thingsclient.vo.ScannedDevice;

import java.util.List;

import javax.inject.Inject;

public class ScanViewModel extends ViewModel {

    private final LiveData<List<ScannedDevice>> results;

    @Inject
    ScanViewModel() {
        results = AbsentLiveData.create();
    }

    LiveData<List<ScannedDevice>> getScanResults() {
        return results;
    }
}
