package com.rosterloh.andriot.ui.dash;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.rosterloh.andriot.repository.WeatherRepository;
import com.rosterloh.andriot.sensors.SensorHub;
import com.rosterloh.andriot.vo.Sensors;
import com.rosterloh.andriot.vo.Weather;
import com.rosterloh.things.common.AppExecutors;
import com.rosterloh.things.common.vo.Resource;

import org.threeten.bp.LocalDateTime;

import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;

public class DashViewModel extends ViewModel {

    private static final int POLL_RATE = 5 * 60 * 1000;

    private final AppExecutors appExecutors;
    private final SensorHub sensorHub;

    private final MutableLiveData<Sensors> sensors = new MutableLiveData<>();
    private final LiveData<Resource<Weather>> weather;

    private LocalDateTime lastWeatherUpdate;

    @Inject
    DashViewModel(WeatherRepository weatherRepository, AppExecutors appExecutors, SensorHub sensorHub) {
        this.appExecutors = appExecutors;
        this.sensorHub = sensorHub;
        weather = weatherRepository.loadWeather();
        lastWeatherUpdate = LocalDateTime.now();

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                appExecutors.diskIO().execute(() -> {
                    float[] data = sensorHub.getSensorData();
                    appExecutors.mainThread().execute(() -> {
                        sensors.setValue(new Sensors(data[0], data[1], null, null, null));
                    });
                });
                if (lastWeatherUpdate.isBefore(LocalDateTime.now().minusMinutes(30))) {
                    lastWeatherUpdate = LocalDateTime.now();
                    // update
                }
            }
        }, 0, POLL_RATE);

    }

    LiveData<Boolean> getMotion() {
        return sensorHub.pirData;
    }

    LiveData<Sensors> getSensorData() {
        return sensors;
    }

    LiveData<Resource<Weather>> getWeather() {
        return weather;
    }
}
