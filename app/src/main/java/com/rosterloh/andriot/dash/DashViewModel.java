package com.rosterloh.andriot.dash;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;

import com.rosterloh.andriot.BR;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.utils.AppPreference;
import com.rosterloh.andriot.utils.WeatherUtils;
import com.rosterloh.andriot.weather.Weather;

import java.net.InetAddress;

/**
 * Exposes the data to be used in the {@link DashContract.View}.
 * <p>
 * {@link BaseObservable} implements a listener registration mechanism which is notified when a
 * property changes. This is done by assigning a {@link Bindable} annotation to the property's
 * getter method.
 */
public class DashViewModel extends BaseObservable {

    boolean isLoading = false;

    @Bindable
    public String weatherIcon;
    @Bindable
    public String temperature;
    @Bindable
    public String description;
    @Bindable
    public String lastUpdate;

    private InetAddress ethIp;
    private boolean ethernetConnected = false;
    private InetAddress wifiIp;
    private String wifiName;
    private boolean wifiConnected = false;

    private final DashContract.Presenter presenter;

    private Context context;

    public DashViewModel(Context context, DashContract.Presenter presenter) {
        this.context = context;
        this.presenter = presenter;
    }

    @Bindable
    public boolean getLoadingVisible() {
        return isLoading;
    }

    public void setLoading(boolean active) {
        isLoading = active;
        notifyPropertyChanged(BR.loadingVisible);
    }

    public void setWeather(Weather weather) {

        AppPreference.saveLastUpdateTimeMillis(context);

        weatherIcon = WeatherUtils.getStrIcon(context, weather.getIconId());
        temperature = weather.getTemp();
        description = weather.getDescription();
        lastUpdate = context.getString(R.string.last_update_label, weather.getLastUpdated());

        notifyPropertyChanged(BR.weatherIcon);
        notifyPropertyChanged(BR.temperature);
        notifyPropertyChanged(BR.description);
        notifyPropertyChanged(BR.lastUpdate);
    }

    @Bindable
    public boolean getEthernetConnected() {
        return ethernetConnected;
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
        ethernetConnected = true;
        notifyPropertyChanged(BR.ethernet);
    }

    public void setWifiIp(InetAddress ip) {
        wifiIp = ip;
        wifiConnected = true;
        notifyPropertyChanged(BR.wifi);
    }

    public void setWifiName(String ssid) {
        wifiName = ssid;
        wifiConnected = true;
        notifyPropertyChanged(BR.wifi);
    }

    @Bindable
    public boolean getWifiConnected() {
        return wifiConnected;
    }

    @Bindable
    public String getWifi() {
        return "WiFi SSID: " + wifiName + " IP: " + wifiIp.getHostAddress();
    }
}
