package com.rosterloh.andriot.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.rosterloh.andriot.api.WeatherResponse;
import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.vo.Weather;
import com.rosterloh.things.common.AppExecutors;
import com.rosterloh.things.common.api.ApiResponse;
import com.rosterloh.things.common.repository.NetworkBoundResource;
import com.rosterloh.things.common.vo.Resource;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Repository that handles Weather objects.
 */
@Singleton
public class WeatherRepository {

    private static final String TAG = WeatherRepository.class.getSimpleName();

    private final WeatherDao weatherDao;
    private final WeatherService weatherService;
    private final AppExecutors appExecutors;

    private static final String LAT = "51.621203";
    private static final String LONG = "-1.294148";
    private static final String KEY = "2e9e498e77b879ea237e3a571c57f1fa";
    private static final String TYPE = "metric";

    @Inject
    WeatherRepository(AppExecutors appExecutors, WeatherDao weatherDao, WeatherService weatherService) {
        this.appExecutors = appExecutors;
        this.weatherDao = weatherDao;
        this.weatherService = weatherService;
    }

    public LiveData<Resource<Weather>> loadWeather() {
        return new NetworkBoundResource<Weather, WeatherResponse>(appExecutors) {
            @Override
            protected void saveCallResult(@NonNull WeatherResponse item) {
                String update = new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date((long) item.getDt() * 1000));
                Weather weather = new Weather(
                        item.getId(),
                        item.getWeather().get(0).getIcon(),
                        item.getMain().getTemp(),
                        item.getWeather().get(0).getDescription(),
                        item.getDt(),
                        update);
                weatherDao.insert(weather);
            }

            @Override
            protected boolean shouldFetch(@Nullable Weather data) {
                if (data == null) {
                    Log.d(TAG, "No data in database. Fetching new");
                    return true;
                }
                if (new DateTime(data.lastUpdate).isBefore(new DateTime().minusMinutes(30))) {
                    Log.d(TAG, "Data in database too old. Refreshing");
                    return true;
                } else {
                    return false;
                }
            }

            @NonNull
            @Override
            protected LiveData<Weather> loadFromDb() {
                return weatherDao.load();
            }

            @NonNull
            @Override
            protected LiveData<ApiResponse<WeatherResponse>> createCall() {
                return weatherService.getWeather(LAT, LONG, KEY, TYPE);
            }
        }.asLiveData();
    }
}
