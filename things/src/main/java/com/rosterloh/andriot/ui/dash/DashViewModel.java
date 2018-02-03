package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.rosterloh.andriot.db.SensorData;
import com.rosterloh.andriot.db.SensorsRepository;
import com.rosterloh.andriot.db.WeatherRepository;
import com.rosterloh.andriot.db.Weather;

import java.util.List;

public class DashViewModel extends ViewModel {

    private final LiveData<Weather> mWeather;
    private final LiveData<SensorData> mSensorData;
    private final LiveData<List<SensorData>> mSensorDataList;

    DashViewModel(WeatherRepository weatherRepository, SensorsRepository sensorsRepository) {
        mWeather = weatherRepository.loadWeather();
        mSensorData = sensorsRepository.getCurrentValue();
        mSensorDataList = sensorsRepository.loadValues();
    }

    LiveData<Weather> getWeather() {
        return mWeather;
    }

    LiveData<SensorData> getSensorData() {
        return mSensorData;
    }

    LiveData<List<SensorData>> getSensorDataList() {
        return mSensorDataList;
    }
}
