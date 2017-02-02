package com.rosterloh.andriot.dash;

import com.rosterloh.andriot.data.ConnectionDetector;
import com.rosterloh.andriot.networking.WeatherRequestManager;
import com.rosterloh.andriot.weather.Weather;
import com.rosterloh.andriot.weather.WeatherResponse;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Notification;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import io.reactivex.observers.DisposableMaybeObserver;

/**
 * Listens to user actions from the UI ({@link DashActivity}), retrieves the data and updates the
 * UI as required.
 */
public class DashPresenter implements DashContract.Presenter {

    private final ConnectionDetector connectionDetector;

    private final DashContract.View dashView;

    private WeatherRequestManager weatherRequestManager;

    public DashPresenter(ConnectionDetector connectionDetector,
                         WeatherRequestManager weatherRequestManager, DashContract.View view) {

        this.connectionDetector = connectionDetector;
        this.weatherRequestManager = weatherRequestManager;
        dashView = view;

        view.setPresenter(this);
    }

    @Override
    public void onViewResumed() {

        dashView.setNetworkInfo(connectionDetector);

        //

        if (!weatherRequestManager.isRequestingInformation()) {

            Observable.interval(0, 30, TimeUnit.MINUTES)
                    .doOnEach(new Consumer<Notification<Long>>() {
                        @Override
                        public void accept(Notification<Long> longNotification) throws Exception {
                            weatherRequestManager.getWeather().subscribe(new WeatherObserver());
                        }
                    })
                    .subscribe();
        }
    }

    @Override
    public void onViewDetached() {

        if (weatherRequestManager.isRequestingInformation()) {
            // clear request
        }
    }

    @Override
    public void result(int requestCode, int resultCode) {

    }

    private class WeatherObserver extends DisposableMaybeObserver<WeatherResponse> {

        @Override
        public void onSuccess(WeatherResponse response) {

            Weather weather = new Weather.Builder()
                    .temperature(response.getMain().getTemp().intValue() + "º")
                    .description(response.getWeather().get(0).getDescription())
                    .iconId(response.getWeather().get(0).getIcon())
                    .lastUpdated(new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date((long) response.getDt() * 1000)))
                    .build();

            dashView.showWeather(weather);
        }

        @Override
        public void onError(Throwable t) {
            dashView.showLoadingError();
        }

        @Override
        public void onComplete() {
            dashView.setLoadingIndicator(false);
        }
    }
}
