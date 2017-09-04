package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.LifecycleRegistryOwner;
import android.arch.lifecycle.Observer;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class LiveDataBus {

    private static SparseArray<EventLiveData> sSubjectMap = new SparseArray<>();

    public static final int SUBJECT_MQTT_DATA = 0;
    public static final int SUBJECT_DOWNLOAD_COMPLETE = 1;

    @Retention(SOURCE)
    @IntDef({SUBJECT_MQTT_DATA, SUBJECT_DOWNLOAD_COMPLETE})
    @interface Subject {
    }

    private LiveDataBus() {
        // hidden constructor
    }

    /**
     * Get the live data or create it if it's not already in memory.
     */
    @NonNull
    private static EventLiveData getLiveData(@Subject int subjectCode) {
        EventLiveData liveData = sSubjectMap.get(subjectCode);
        if (liveData == null) {
            liveData = new EventLiveData(subjectCode);
            sSubjectMap.put(subjectCode, liveData);
        }

        return liveData;
    }

    /**
     * Subscribe to the specified subject and listen for updates on that subject.
     */
    public static void subscribe(@Subject int subject, @NonNull LifecycleRegistryOwner lifecycle, @NonNull Observer<Object> action) {
        getLiveData(subject).observe(lifecycle, action);
    }

    /**
     * Subscribe to the specified subject and listen for updates on that subject.
     */
    public static void subscribe(@Subject int subject, @NonNull Observer<Object> action) {
        getLiveData(subject).observeForever(action);
    }

    /**
     * Removes this subject when it has no observers.
     */
    public static void unregister(@Subject int subject) {
        sSubjectMap.remove(subject);
    }

    /**
     * Publish an object to the specified subject for all subscribers of that subject.
     */
    public static void publish(@Subject int subject, @NonNull Object message) {
        getLiveData(subject).update(message);
    }
}
