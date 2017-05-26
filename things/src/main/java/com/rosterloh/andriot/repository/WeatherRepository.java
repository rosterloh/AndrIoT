package com.rosterloh.andriot.repository;

import android.arch.lifecycle.LiveData;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.rosterloh.andriot.api.WeatherResponse;
import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.vo.Weather;
import com.rosterloh.things.common.AppExecutors;
import com.rosterloh.things.common.api.ApiResponse;
import com.rosterloh.things.common.repository.NetworkBoundResource;
import com.rosterloh.things.common.vo.Resource;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.ChronoUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

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

                // FIXME: This is not working for some reason
                LocalDateTime date =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(item.getDt()), ZoneOffset.UTC);

                Weather weather = new Weather(
                        item.getId(),
                        item.getWeather().get(0).getIcon(),
                        item.getMain().getTemp(),
                        item.getWeather().get(0).getDescription(),
                        LocalDateTime.now());
                weatherDao.insert(weather);
            }

            @Override
            protected boolean shouldFetch(@Nullable Weather data) {
                if (data == null) {
                    Timber.d("No data in database. Fetching new");
                    return true;
                }

                if (ChronoUnit.MINUTES.between(LocalDateTime.now(), data.lastUpdate) > 30) {
                    Timber.d("Data in database too old. Refreshing");
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
