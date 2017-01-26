package com.rosterloh.andriot.dash;

import com.rosterloh.andriot.weather.Weather;

import java.net.InetAddress;
import java.util.Map;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface DashContract {

    interface View {

        void setPresenter(Presenter presenter);

        void setLoadingIndicator(boolean active);

        void showWeather(Weather weather);

        void setNetworkInfo(Map<String, InetAddress> ips);

        void showLoadingError();

        void showSuccessMessage();

        boolean isActive();
    }

    interface Presenter {

        void start();

        void result(int requestCode, int resultCode);
    }
}
