package com.rosterloh.andriot.binding;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import com.rosterloh.andriot.R;

/**
 * Data Binding adapters specific to the app.
 */
public class BindingAdapters {

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("weatherIcon")
    public static void convertIcon(TextView textView, String iconId) {

        if (iconId != null) {
            int res;

            switch (iconId) {
                case "01d":
                    res = R.string.icon_clear_sky_day;
                    break;
                case "01n":
                    res = R.string.icon_clear_sky_night;
                    break;
                case "02d":
                    res = R.string.icon_few_clouds_day;
                    break;
                case "02n":
                    res = R.string.icon_few_clouds_night;
                    break;
                case "03d":
                    res = R.string.icon_scattered_clouds;
                    break;
                case "03n":
                    res = R.string.icon_scattered_clouds;
                    break;
                case "04d":
                    res = R.string.icon_broken_clouds;
                    break;
                case "04n":
                    res = R.string.icon_broken_clouds;
                    break;
                case "09d":
                    res = R.string.icon_shower_rain;
                    break;
                case "09n":
                    res = R.string.icon_shower_rain;
                    break;
                case "10d":
                    res = R.string.icon_rain_day;
                    break;
                case "10n":
                    res = R.string.icon_rain_night;
                    break;
                case "11d":
                    res = R.string.icon_thunderstorm;
                    break;
                case "11n":
                    res = R.string.icon_thunderstorm;
                    break;
                case "13d":
                    res = R.string.icon_snow;
                    break;
                case "13n":
                    res = R.string.icon_snow;
                    break;
                case "50d":
                    res = R.string.icon_mist;
                    break;
                case "50n":
                    res = R.string.icon_mist;
                    break;
                default:
                    res = R.string.icon_weather_default;
            }

            textView.setText(res);
        }
    }
}
