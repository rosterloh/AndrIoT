package com.rosterloh.andriot.dash;

import android.support.annotation.NonNull;

import com.rosterloh.andriot.data.DataRepository;
import com.rosterloh.andriot.weather.Weather;

import io.reactivex.functions.Consumer;

/**
 * Listens to user actions from the UI ({@link DashActivity}), retrieves the data and updates the
 * UI as required.
 */
public class DashPresenter implements DashContract.Presenter {

    private final DataRepository dataRepository;

    private final DashContract.View dashView;

    public DashPresenter(@NonNull DataRepository repository, DashContract.View view) {

        dataRepository = repository;
        dashView = view;

        view.setPresenter(this);
    }

    @Override
    public void start() {

        dashView.setNetworkInfo(dataRepository.getConnectionDetector().getIpAddresses());

        dataRepository.getWeather().subscribe(new Consumer<Weather>() {
            @Override
            public void accept(Weather weather) throws Exception {

                if (!dashView.isActive()) {
                    return;
                }
                dashView.setLoadingIndicator(true);
                dashView.showWeather(weather);
                dashView.setLoadingIndicator(false);
            }
        }, new Consumer<Throwable>() {
            @Override
            public void accept(Throwable throwable) throws Exception {
                dashView.showLoadingError();
            }
        });
    }

    @Override
    public void result(int requestCode, int resultCode) {

    }
}
