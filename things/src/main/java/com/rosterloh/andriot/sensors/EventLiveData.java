package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;

public class EventLiveData extends LiveData<Object> {

    private final int mSubject;

    public EventLiveData(@LiveDataBus.Subject int subject) {
        mSubject = subject;
    }

    public void update(Object object) {
        postValue(object);
    }

    @Override
    public void removeObserver(Observer<Object> observer) {
        super.removeObserver(observer);
        if (!hasObservers()) {
            LiveDataBus.unregister(mSubject);
        }
    }
}
