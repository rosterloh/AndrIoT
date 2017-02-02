package com.rosterloh.andriot.dash;

import com.rosterloh.andriot.data.ConnectionDetector;
import com.rosterloh.andriot.weather.Weather;

/**
 * This specifies the contract between the view and the presenter.
 */
public interface DashContract {

    interface View {

        void setPresenter(Presenter presenter);

        void setLoadingIndicator(boolean active);

        void showWeather(Weather weather);

        void setNetworkInfo(ConnectionDetector network);

        void showLoadingError();

        void showSuccessMessage();

        boolean isActive();
    }

    interface Presenter {

        void onViewResumed();

        void onViewDetached();

        void result(int requestCode, int resultCode);
    }
}
