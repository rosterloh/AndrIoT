package com.rosterloh.andriot.ui.dash;

import com.rosterloh.andriot.db.SensorsRepository;
import com.rosterloh.andriot.db.WeatherRepository;

import dagger.Module;
import dagger.Provides;

@Module
public class DashModule {

    @Provides
    DashViewModelFactory provideDashViewModelFactory(WeatherRepository weatherRepository, SensorsRepository sensorsRepository) {
        return new DashViewModelFactory(weatherRepository, sensorsRepository);
    }
}
