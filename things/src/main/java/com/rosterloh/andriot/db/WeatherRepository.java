package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.rosterloh.andriot.api.WeatherResponse;
import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.AppExecutors;

import java.time.LocalDateTime;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Repository that handles Weather objects.
 */
@Singleton
public class WeatherRepository {

    private static final String KEY = "2e9e498e77b879ea237e3a571c57f1fa";
    private static final String TYPE = "metric";

    private final WeatherDao mWeatherDao;
    private final WeatherService mWeatherService;
    private final AppExecutors mAppExecutors;

    private final MediatorLiveData<Weather> mWeatherData = new MediatorLiveData<>();
    private final LiveData<Weather> mDbData;

    private LocalSettings mSettings;
    private Timer mTimer;

    @Inject
    WeatherRepository(AppExecutors appExecutors, WeatherDao weatherDao, WeatherService weatherService,
                      SettingsRepository settingsRepository) {
        mAppExecutors = appExecutors;
        mWeatherDao = weatherDao;
        mWeatherService = weatherService;

        LiveData<LocalSettings> settingsData = settingsRepository.getLocalSettings();
        settingsData.observeForever(settings -> {
            if (settings != null) {
                Timber.d("Settings changed. Refreshing data.");
                mSettings = settings;
                startDataRefresh();
            }
        });

        mDbData = weatherDao.load();
        mWeatherData.addSource(mDbData, data -> {
            mWeatherData.removeSource(mDbData);
            if (mDbData.getValue() != null) {
                mWeatherData.addSource(mDbData, mWeatherData::setValue);
            } else {
                getFromNetwork();
            }
        });
    }

    public LiveData<Weather> loadWeather() {
        return mWeatherData;
    }

    private void getFromNetwork() {
        if (mSettings != null) {
            Call<WeatherResponse> call = mWeatherService.getWeather(Double.toString(mSettings.getLatitude()),
                    Double.toString(mSettings.getLongitude()), KEY, TYPE);
            call.enqueue(new Callback<WeatherResponse>() {

                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    Weather weather = new Weather(response.body());

                    mAppExecutors.diskIO().execute(() -> {
                        mWeatherDao.insert(weather);
                        mAppExecutors.mainThread().execute(()
                                -> mWeatherData.addSource(mWeatherDao.load(), mWeatherData::setValue));
                    });
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    Timber.e("Failed to get weather: " + t.getMessage());
                    //weatherData.addSource(mDbData, weatherData::setValue);
                }
            });
        } else {
            Timber.w("Can't get weather. Check local settings");
        }
    }

    private boolean dataNeedsRefresh() {
        return ((mDbData.getValue() == null)
                || (mDbData.getValue().getLastUpdate().isBefore(LocalDateTime.now().minusMinutes(30))));
    }

    private void startDataRefresh() {
        if (mTimer != null) {
            mTimer.cancel();
        }
        mTimer = new Timer();
        mTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (dataNeedsRefresh()) {
                    Timber.d("Refreshing weather data");
                    getFromNetwork();
                } else {
                    Timber.d("Refresh not needed. Waiting for next cycle");
                }
            }
        }, 0, mSettings.getRefreshRate());
    }
}
