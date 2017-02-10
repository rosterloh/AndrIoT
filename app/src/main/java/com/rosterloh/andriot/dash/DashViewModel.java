package com.rosterloh.andriot.dash;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.ObservableBoolean;
import android.databinding.ObservableField;

import com.rosterloh.andriot.BR;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.data.ConnectionDetector;
import com.rosterloh.andriot.networking.WeatherRequestManager;
import com.rosterloh.andriot.ui.SnackbarChangedCallback;
import com.rosterloh.andriot.utils.AppPreference;
import com.rosterloh.andriot.utils.WeatherUtils;
import com.rosterloh.andriot.weather.Weather;
import com.rosterloh.andriot.weather.WeatherResponse;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.observers.DisposableMaybeObserver;

/**
 * Exposes the data to be used in the dashboard screen.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class DashViewModel extends BaseObservable implements SnackbarChangedCallback.SnackBarViewModel {

    public final ObservableBoolean isLoading = new ObservableBoolean(false);

    public final ObservableField<String> weatherIcon = new ObservableField<>();
    public final ObservableField<String> temperature = new ObservableField<>();
    public final ObservableField<String> description = new ObservableField<>();
    public final ObservableField<String> lastUpdate = new ObservableField<>();

    private InetAddress ethIp;
    public final ObservableBoolean ethernetConnected = new ObservableBoolean(false);
    private InetAddress wifiIp;
    private String wifiName;
    public final ObservableBoolean wifiConnected = new ObservableBoolean(false);

    // This is a special Observable that will trigger a SnackBarChangedCallback when modified.
    final ObservableField<String> snackbarText = new ObservableField<>();

    private final ConnectionDetector connectionDetector;

    private WeatherRequestManager weatherRequestManager;

    private final DashNavigator navigator;

    private Context context; // To avoid leaks, this must be an Application Context.

    public DashViewModel(Context context, DashNavigator navigator) {

        this.context = context.getApplicationContext();
        this.connectionDetector = ConnectionDetector.getInstance(context);
        this.weatherRequestManager = WeatherRequestManager.getInstance(context);
        this.navigator = navigator;
    }

    public void onViewResumed() {

        Map<String, InetAddress> ips = connectionDetector.getIpAddresses();
        if(ips.containsKey("eth0"))
            setEthIp(ips.get("eth0"));

        if(ips.containsKey("wlan0")) {
            setWifiIp(ips.get("wlan0"));
            setWifiName(connectionDetector.getWifiSSid());
        }

        if (!weatherRequestManager.isRequestingInformation()) {

            Observable.interval(0, 30, TimeUnit.MINUTES)
                    .doOnEach(ignore -> weatherRequestManager.getWeather().subscribe(new WeatherObserver()))
                    .subscribe();
        }
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

            setWeather(weather);
        }

        @Override
        public void onError(Throwable t) {
            snackbarText.set("Error loading data");
        }

        @Override
        public void onComplete() {
            setLoading(false);
        }
    }


    public void onViewDetached() {
        if (weatherRequestManager.isRequestingInformation()) {
            // clear request
        }
    }

    void handleActivityResult(int requestCode, int resultCode) {

    }

    void showForecast() {
        // Open the forecast dialog here
    }

    @Override
    public String getSnackbarText() {
        return snackbarText.get();
    }

    public void setLoading(boolean active) {
        isLoading.set(active);
    }

    public void setWeather(Weather weather) {

        AppPreference.saveLastUpdateTimeMillis(context);

        weatherIcon.set(WeatherUtils.getStrIcon(context, weather.getIconId()));
        temperature.set(weather.getTemp());
        description.set(weather.getDescription());
        lastUpdate.set(context.getString(R.string.last_update_label, weather.getLastUpdated()));
    }

    @Bindable
    public String getEthernet() {
        if (ethIp == null) {
            return "No Ethernet Connection";
        } else {
            return "Ethernet IP: " + ethIp.getHostAddress();
        }
    }

    public void setEthIp(InetAddress ip) {
        ethIp = ip;
        ethernetConnected.set(true);
        notifyPropertyChanged(BR.ethernet);
    }

    public void setWifiIp(InetAddress ip) {
        wifiIp = ip;
        wifiConnected.set(true);
        notifyPropertyChanged(BR.wifi);
    }

    public void setWifiName(String ssid) {
        wifiName = ssid;
        wifiConnected.set(true);
        notifyPropertyChanged(BR.wifi);
    }

    @Bindable
    public String getWifi() {
        return "WiFi SSID: " + wifiName + " IP: " + wifiIp.getHostAddress();
    }
}
