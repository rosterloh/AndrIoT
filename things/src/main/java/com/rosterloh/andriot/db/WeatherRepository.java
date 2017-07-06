package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import com.rosterloh.andriot.api.WeatherResponse;
import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.AppExecutors;

import org.threeten.bp.LocalDateTime;

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

    private final WeatherDao weatherDao;
    private final WeatherService weatherService;
    private final AppExecutors appExecutors;

    private static final String LAT = "51.621203";
    private static final String LONG = "-1.294148";
    private static final String KEY = "2e9e498e77b879ea237e3a571c57f1fa";
    private static final String TYPE = "metric";

    private static final int POLL_RATE = 30 * 60 * 1000;
    private static final int INIT_DELAY = 5 * 1000;

    private final MediatorLiveData<Weather> weatherData = new MediatorLiveData<>();

    @Inject
    WeatherRepository(AppExecutors appExecutors, WeatherDao weatherDao, WeatherService weatherService) {
        this.appExecutors = appExecutors;
        this.weatherDao = weatherDao;
        this.weatherService = weatherService;

        LiveData<Weather> dbData = weatherDao.load();
        weatherData.addSource(dbData, data -> {
            weatherData.removeSource(dbData);
            if (dbData.getValue() != null) {
                weatherData.addSource(dbData, weatherData::setValue);
            } else {
                getFromNetwork(dbData);
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                if (dataNeedsRefresh(dbData)) {
                    Timber.d("Refreshing weather data");
                    getFromNetwork(dbData);
                }
            }
        }, INIT_DELAY, POLL_RATE);
    }

    public LiveData<Weather> loadWeather() {
        return weatherData;
    }

    private void getFromNetwork(final LiveData<Weather> dbData) {
        Call<WeatherResponse> call = weatherService.getWeather(LAT, LONG, KEY, TYPE);
        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Weather weather = new Weather(response.body());

                appExecutors.diskIO().execute(() ->  {
                    weatherDao.insert(weather);
                    appExecutors.mainThread().execute(() -> weatherData.addSource(weatherDao.load(), weatherData::setValue));
                });
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Timber.e("Failed to get weather: " + t.getMessage());
                weatherData.addSource(dbData, weatherData::setValue);
            }
        });
    }

    private boolean dataNeedsRefresh(final LiveData<Weather> dbData) {
        return ((dbData.getValue() == null) ||
                (dbData.getValue().lastUpdate.isBefore(LocalDateTime.now().minusMinutes(30))));
    }
}
