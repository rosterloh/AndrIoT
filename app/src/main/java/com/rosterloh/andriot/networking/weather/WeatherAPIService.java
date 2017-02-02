package com.rosterloh.andriot.networking.weather;

import com.rosterloh.andriot.data.PrivateSharedPreferencesManager;
import com.rosterloh.andriot.weather.WeatherResponse;

import io.reactivex.Maybe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class WeatherAPIService {

    private IWeatherAPI weatherAPI;
    private PrivateSharedPreferencesManager preferences;
    private boolean isRequestingWeather;

    public WeatherAPIService(Retrofit retrofit, PrivateSharedPreferencesManager privateSharedPreferencesManager) {

        weatherAPI =retrofit.create(IWeatherAPI.class);
        preferences = privateSharedPreferencesManager;
    }

    public boolean isRequestingWeather() {
        return isRequestingWeather;
    }

    public Maybe<WeatherResponse> requestWeather() {

        return weatherAPI.getCurrentWeatherConditions("51.621203", "-1.294148", "2e9e498e77b879ea237e3a571c57f1fa", "metric")
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        isRequestingWeather = true;
                    }
                })
                .doOnTerminate(new Action() {
                    @Override
                    public void run() throws Exception {
                        isRequestingWeather = false;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                //.onErrorResumeNext(this::handleError)
                //.doOnNext(this::processResponse)
                .singleElement();

    }
}
