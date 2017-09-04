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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

public class DashViewModel extends ViewModel {

    @Inject
    SensorHub mSensorHub;

    private final LiveData<List<SensorData>> mSensorData;
    private final LiveData<Weather> mWeather;

    @Inject
    DashViewModel(WeatherRepository weatherRepository, SensorsRepository sensorsRepository) {
        mWeather = weatherRepository.loadWeather();
        mSensorData = sensorsRepository.loadValues();
    }

    LiveData<Boolean> getMotion() {
        return mSensorHub.getPirData();
    }

    LiveData<List<SensorData>> getSensorData() {
        return mSensorData;
    }

    LiveData<Weather> getWeather() {
        return mWeather;
    }
}
