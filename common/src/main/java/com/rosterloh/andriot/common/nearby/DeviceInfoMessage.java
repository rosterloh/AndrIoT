package com.rosterloh.andriot.common.nearby;

import android.os.Parcel;
import android.os.Parcelable;

public class DeviceInfoMessage implements Parcelable {

    private String mDeviceId;
    private String mIpAddress;

    public DeviceInfoMessage(String id, String ip) {
        mDeviceId = id;
        mIpAddress = ip;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    @Override
    public String toString() {
        return "DeviceInfo{"
                + "deviceId=" + mDeviceId
                + ", ipAddress=" + mIpAddress
                + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mDeviceId);
        dest.writeString(mIpAddress);
    }

    protected DeviceInfoMessage(Parcel in) {
        mDeviceId = in.readString();
        mIpAddress = in.readString();
    }

    public static final Creator<DeviceInfoMessage> CREATOR = new Creator<DeviceInfoMessage>() {
        @Override
        public DeviceInfoMessage createFromParcel(Parcel in) {
            return new DeviceInfoMessage(in);
        }

        @Override
        public DeviceInfoMessage[] newArray(int size) {
            return new DeviceInfoMessage[size];
        }
    };
}
