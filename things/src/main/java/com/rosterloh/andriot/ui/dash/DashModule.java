package com.rosterloh.andriot.ui.dash;

import android.view.Display;

import com.google.android.things.device.ScreenManager;
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

    @Provides
    ScreenManager provideScreenManager() {
        return ScreenManager.getInstance(Display.DEFAULT_DISPLAY);
    }
}
