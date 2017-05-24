package com.rosterloh.andriot.cloud;

import android.os.Parcel;
import android.os.Parcelable;

public class SensorData implements Parcelable {

    private long timestamp;
    private String sensorName;
    private float value;

    public SensorData(long timestamp, String sensorName, float value) {
        this.timestamp = timestamp;
        this.sensorName = sensorName;
        this.value = value;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getSensorName() {
        return sensorName;
    }

    public float getValue() {
        return value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeLong(timestamp);
        out.writeString(sensorName);
        out.writeFloat(value);
    }

    public static final Parcelable.Creator<SensorData> CREATOR
            = new Parcelable.Creator<SensorData>() {
        public SensorData createFromParcel(Parcel in) {
            return new SensorData(in);
        }

        public SensorData[] newArray(int size) {
            return new SensorData[size];
        }
    };

    private SensorData(Parcel in) {
        this(in.readLong(), in.readString(), in.readFloat());
    }

    @Override
    public String toString() {
        return sensorName + " [" + timestamp + "] " + value;
    }
}
