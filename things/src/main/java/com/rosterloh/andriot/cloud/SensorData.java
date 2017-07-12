package com.rosterloh.andriot.cloud;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {

    public static final Parcelable.Creator<SensorData> CREATOR
            = new Parcelable.Creator<SensorData>() {
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };

    private long mTimestamp;
    private String mSensorName;
    private float mValue;

    private SensorData(Parcel in) {
        this(in.readLong(), in.readString(), in.readFloat());
    }

    public SensorData(long timestamp, String sensorName, float value) {
        mTimestamp = timestamp;
        mSensorName = sensorName;
        mValue = value;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getSensorName() {
        return mSensorName;
    }

    public float getValue() {
        return mValue;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(mTimestamp);
        out.writeString(mSensorName);
        out.writeFloat(mValue);
    }

    @Override
    public String toString() {
        return mSensorName + " [" + mTimestamp + "] " + mValue;
    }
}
