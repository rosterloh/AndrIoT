package com.rosterloh.andriot.repository;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.rosterloh.andriot.api.WeatherResponse;
import com.rosterloh.andriot.api.WeatherService;
import com.rosterloh.andriot.db.WeatherDao;
import com.rosterloh.andriot.vo.Weather;
import com.rosterloh.andriot.AppExecutors;

import org.threeten.bp.Instant;
import org.threeten.bp.LocalDateTime;
import org.threeten.bp.ZoneOffset;
import org.threeten.bp.temporal.ChronoUnit;

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

    @Inject
    WeatherRepository(AppExecutors appExecutors, WeatherDao weatherDao, WeatherService weatherService) {
        this.appExecutors = appExecutors;
        this.weatherDao = weatherDao;
        this.weatherService = weatherService;
    }

    public LiveData<Weather> getWeather() {

        LiveData<Weather> dbData = weatherDao.load();
        if (dbData != null) {

            long time = ChronoUnit.MINUTES.between(dbData.getValue().lastUpdate, LocalDateTime.now());
            if (time > 30L) {
                Timber.d("Data in database too old. Refreshing");
                return getFromNetwork();
            } else {
                Timber.d("DB data time difference " + time);
                return dbData;
            }
        } else {
            return getFromNetwork();
        }
    }

    private LiveData<Weather> getFromNetwork() {
        final MutableLiveData<Weather> liveData = new MutableLiveData<>();
        Call<WeatherResponse> call = weatherService.getWeather(LAT, LONG, KEY, TYPE);
        call.enqueue(new Callback<WeatherResponse>() {

            @Override
            public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                WeatherResponse item = response.body();
                LocalDateTime date =
                        LocalDateTime.ofInstant(Instant.ofEpochMilli(item.getDt()), ZoneOffset.UTC);

                Weather weather = new Weather(
                        item.getId(),
                        item.getWeather().get(0).getIcon(),
                        item.getMain().getTemp(),
                        item.getWeather().get(0).getDescription(),
                        LocalDateTime.now());
                weatherDao.insert(weather);
                liveData.setValue(weather);
            }

            @Override
            public void onFailure(Call<WeatherResponse> call, Throwable t) {
                Timber.e("Failed to get weather: " + t.getMessage());
                liveData.setValue(null);
            }
        });
        return liveData;
    }
}
