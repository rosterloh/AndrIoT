package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.rosterloh.andriot.db.SensorsRepository;
import com.rosterloh.andriot.db.WeatherRepository;

public class DashViewModelFactory implements ViewModelProvider.Factory {

    private final WeatherRepository weatherRepository;
    private final SensorsRepository sensorsRepository;

    DashViewModelFactory(WeatherRepository weatherRepository, SensorsRepository sensorsRepository) {
        this.weatherRepository = weatherRepository;
        this.sensorsRepository = sensorsRepository;
    }

    @Override
    public <T extends ViewModel> T create(Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DashViewModel.class)) {
            return (T) new DashViewModel(weatherRepository, sensorsRepository);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}
