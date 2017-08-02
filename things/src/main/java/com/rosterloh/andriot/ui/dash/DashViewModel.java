package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.rosterloh.andriot.db.SensorData;
import com.rosterloh.andriot.db.SensorsRepository;
import com.rosterloh.andriot.db.WeatherRepository;
import com.rosterloh.andriot.sensors.SensorHub;
import com.rosterloh.andriot.db.Weather;
import com.rosterloh.andriot.AppExecutors;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

public class DashViewModel extends ViewModel {

    private static final int POLL_RATE = 5 * 60 * 1000;
    private static final int INIT_DELAY = 5 * 1000;

    private final AppExecutors mAppExecutors;
    private final SensorsRepository mSensorsRepository;

    @Inject
    SensorHub mSensorHub;

    private final MutableLiveData<SensorData> mSensors = new MutableLiveData<>();
    private final LiveData<Weather> mWeather;

    @Inject
    DashViewModel(WeatherRepository weatherRepository, AppExecutors appExecutors,
                  SensorsRepository sensorsRepository) {
        mAppExecutors = appExecutors;
        mSensorsRepository = sensorsRepository;
        mWeather = weatherRepository.loadWeather();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                appExecutors.diskIO().execute(() -> {
                    SensorData data = mSensorHub.getSensorData();
                    if (data != null) {
                        appExecutors.mainThread().execute(() -> mSensors.setValue(data));
                    }
                });
            }
        }, INIT_DELAY, POLL_RATE);
    }

    LiveData<Boolean> getMotion() {
        return mSensorHub.getPirData();
    }

    LiveData<SensorData> getSensorData() {
        return mSensors;
    }

    LiveData<Weather> getWeather() {
        return mWeather;
    }
}
