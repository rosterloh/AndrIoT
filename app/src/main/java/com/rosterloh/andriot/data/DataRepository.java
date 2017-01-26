package com.rosterloh.andriot.data;

import android.content.Context;
import android.support.annotation.NonNull;

import com.rosterloh.andriot.ConnectionDetector;
import com.rosterloh.andriot.weather.Weather;
import com.rosterloh.andriot.weather.WeatherApi;
import com.rosterloh.andriot.weather.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Concrete implementation to load data from the data sources into a cache.
 */
public class DataRepository implements DataSource {

    private static DataRepository INSTANCE = null;

    private ConnectionDetector connectionDetector;
    //private Context context;

    // Prevent direct instantiation.
    private DataRepository(@NonNull Context context) {
        connectionDetector = new ConnectionDetector(context);
        //this.context = context;
    }

    /**
     * Returns the single instance of this class, creating it if necessary.
     *
     * @return the {@link DataRepository} instance
     */
    public static DataRepository getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new DataRepository(context);
        }
        return INSTANCE;
    }

    /**
     * Used to force {@link #getInstance(Context)} to create a new instance
     * next time it's called.
     */
    public static void destroyInstance() {
        INSTANCE = null;
    }

    /**
     * Gets weather from remote data source
     */
    @Override
    public Observable<Weather> getWeather() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        final WeatherApi weatherApi = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(WeatherApi.class);

        return Observable.interval(0, 20, TimeUnit.MINUTES)
                .flatMap(new Function<Long, Observable<WeatherResponse>>() {
                    @Override
                    public Observable<WeatherResponse> apply(Long aLong) throws Exception {
                        return weatherApi.getCurrentWeatherConditions("51.621203", "-1.294148", "2e9e498e77b879ea237e3a571c57f1fa", "metric");
                    }
                })
                .flatMap(new Function<WeatherResponse, Observable<Weather>>() {
                    @Override
                    public Observable<Weather> apply(WeatherResponse response) throws Exception {
                        return Observable.just(new Weather.Builder()
                                .temperature(response.getMain().getTemp().intValue() + "º")
                                .description(response.getWeather().get(0).getDescription())
                                .iconId(response.getWeather().get(0).getIcon())
                                .lastUpdated(new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date((long) response.getDt() * 1000)))
                                .build());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io());
    }

    public ConnectionDetector getConnectionDetector() {
        return connectionDetector;
    }
}
