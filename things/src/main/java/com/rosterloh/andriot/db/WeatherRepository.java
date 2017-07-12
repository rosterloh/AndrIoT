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

    private static final String LAT = "51.621203";
    private static final String LONG = "-1.294148";
    private static final String KEY = "2e9e498e77b879ea237e3a571c57f1fa";
    private static final String TYPE = "metric";

    private static final int POLL_RATE = 30 * 60 * 1000;
    private static final int INIT_DELAY = 5 * 1000;

    private final WeatherDao mWeatherDao;
    private final WeatherService mWeatherService;
    private final AppExecutors mAppExecutors;

    private final MediatorLiveData<Weather> mWeatherData = new MediatorLiveData<>();

    @Inject
    WeatherRepository(AppExecutors appExecutors, WeatherDao weatherDao, WeatherService weatherService) {
        mAppExecutors = appExecutors;
        mWeatherDao = weatherDao;
        mWeatherService = weatherService;

        LiveData<Weather> dbData = weatherDao.load();
        mWeatherData.addSource(dbData, data -> {
            mWeatherData.removeSource(dbData);
            if (dbData.getValue() != null) {
                mWeatherData.addSource(dbData, mWeatherData::setValue);
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
        return mWeatherData;
    }

    private void getFromNetwork(final LiveData<Weather> dbData) {
        Call<WeatherResponse> call = mWeatherService.getWeather(LAT, LONG, KEY, TYPE);
        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                Weather weather = new Weather(response.body());

                mAppExecutors.diskIO().execute(() ->  {
                    mWeatherDao.insert(weather);
                    mAppExecutors.mainThread().execute(()
                            -> mWeatherData.addSource(mWeatherDao.load(), mWeatherData::setValue));
                });
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Timber.e("Failed to get weather: " + t.getMessage());
                //weatherData.addSource(dbData, weatherData::setValue);
            }
        });
    }

    private boolean dataNeedsRefresh(final LiveData<Weather> dbData) {
        return ((dbData.getValue() == null)
                || (dbData.getValue().getLastUpdate().isBefore(LocalDateTime.now().minusMinutes(30))));
    }
}
