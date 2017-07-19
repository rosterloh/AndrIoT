package com.rosterloh.andriot.common.nearby;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

public class LocationMessage implements Parcelable {

    private double mLatitude;
    private double mLongitude;

    public LocationMessage(Location location) {
        mLatitude = location.getLatitude();
        mLongitude = location.getLongitude();
    }

    public LocationMessage(double latitude, double longitude) {
        mLatitude = latitude;
        mLongitude = longitude;
    }

    public double getLatitude() {
        return mLatitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    @Override
    public String toString() {
        return "Location{"
                + "latitude=" + mLatitude
                + ", longitude=" + mLongitude
                + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(mLatitude);
        dest.writeDouble(mLongitude);
    }

    protected LocationMessage(Parcel in) {
        mLatitude = in.readDouble();
        mLongitude = in.readDouble();
    }

    public static final Creator<LocationMessage> CREATOR = new Creator<LocationMessage>() {
        @Override
        public LocationMessage createFromParcel(Parcel in) {
            return new LocationMessage(in);
        }

        @Override
        public LocationMessage[] newArray(int size) {
            return new LocationMessage[size];
        }
    };
}
