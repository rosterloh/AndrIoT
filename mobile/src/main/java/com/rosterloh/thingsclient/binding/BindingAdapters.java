package com.rosterloh.thingsclient.binding;

import android.databinding.BindingAdapter;
import android.view.View;
import android.widget.TextView;

import com.rosterloh.andriot.common.nearby.BaseNearby;

/**
 * Data Binding adapters specific to the app.
 */
public class BindingAdapters {

    @BindingAdapter("visibleGone")
    public static void showHide(View view, boolean show) {
        view.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    @BindingAdapter("nearbyStatus")
    public static void convertStatus(TextView textView, BaseNearby.NearbyStatus status) {

        if (status != null) {
            String msg = "Status: ";

            switch (status) {
                case CONNECTING:
                    msg += "CONNECTING";
                    break;
                case DISCOVERING:
                    msg += "DISCOVERING";
                    break;
                case ADVERTISING:
                    msg += "ADVERTISING";
                    break;
                case REQUESTING:
                    msg += "REQUESTING";
                    break;
                case CONNECTED:
                    msg += "CONNECTED";
                    break;
                case REJECTED:
                    msg += "REJECTED";
                    break;
                case DISCONNECTED:
                    msg += "DISCONNECTED";
                    break;
                case SUSPENDED:
                    msg += "SUSPENDED";
                    break;
                case TRANSFERRING:
                    msg += "TRANSFERRING";
                    break;
                case ERROR:
                    msg += "ERROR";
                    break;
                default:
                    msg += "UNKNOWN";
            }

            textView.setText(msg);
        }
    }
}