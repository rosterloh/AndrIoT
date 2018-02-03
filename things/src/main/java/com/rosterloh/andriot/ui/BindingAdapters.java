package com.rosterloh.andriot.ui;

import android.databinding.BindingAdapter;
import android.graphics.Color;
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

    @BindingAdapter("airQuality")
    public static void displayIaq(TextView textView, float iaq) {
        String msg = String.format("%2.0f: ", iaq);
        //  IAQ classification and color-coding
        //  0 - 50 - good - #00e400
        //  51 - 100 - average - #ffff00
        //  101 - 200 - little bad - #ff7e00
        //  201 - 300 - bad - #ff0000
        //  301 - 400 - worse - #99004c
        //  401 - 500 - very bad - #000000
        if (iaq >= 401.0 && iaq <= 500) {
            msg += "Very bad";
            textView.setBackgroundColor(0x2A000000);
        } else if (iaq >= 301 && iaq <= 400) {
            msg += "Worse";
            textView.setBackgroundColor(0x2A99004C);
        } else if (iaq >= 201 && iaq <= 300) {
            msg += "Bad";
            textView.setBackgroundColor(0x2AFF0000);
        } else if (iaq >= 101 && iaq <= 200) {
            msg += "Little bad";
            textView.setBackgroundColor(0x2AFF7E00);
        } else if (iaq >= 51 && iaq <= 100){
            msg += "Average";
            textView.setBackgroundColor(0x2AFFFF00);
        } else if (iaq >= 0 && iaq <= 50){
            msg += "Good";
            textView.setBackgroundColor(0x2A00E400);
        } else {
            msg += "Unknown";
            textView.setBackgroundColor(0xFA202020);
        }
        msg += " Air Quality";
        textView.setText(msg);
    }
}
