package com.rosterloh.andriot.sensors;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class ConnectionModel {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({NONE, WIFI, MOBILE, ETHERNET})
    public @interface ConnectionType {}
    public static final int NONE = 0;
    public static final int WIFI = 1;
    public static final int MOBILE = 2;
    public static final int ETHERNET = 3;

    private int type;
    private boolean isConnected;

    public ConnectionModel(@ConnectionType int type, boolean isConnected) {
        this.type = type;
        this.isConnected = isConnected;
    }

    public int getType() {
        return type;
    }

    public boolean getIsConnected() {
        return isConnected;
    }
}
