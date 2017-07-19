package com.rosterloh.andriot.common.nearby;

import android.location.Location;
import android.os.Parcel;
import android.os.Parcelable;

/*
public class MessagePayload implements Serializable {

    public final static byte PAYLOAD_TYPE_DEVICE_INFO = 0;
    public final static byte PAYLOAD_TYPE_LOCATION = 1;

    private byte mType;
    private Object mData;

    MessagePayload(byte type, Object data) {
        mType = type;
        mData = data;
    }

    public MessagePayload(Location location) {
        mType = PAYLOAD_TYPE_LOCATION;
        mData = new LocationMessage(location);
    }

    public byte getType() {
        return mType;
    }

    public Object getData() {
        return mData;
    }

    public static byte[] serialise(Object obj) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ObjectOutputStream os = new ObjectOutputStream(out);
        os.writeObject(obj);
        return out.toByteArray();
    }

    public static Object deserialise(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    @Override
    public String toString() {
        return "Message{"
                + "type=" + mType
                + ", data=" + mData.toString()
                + '}';
    }
}
*/

public class MessagePayload implements Parcelable {

    public final static byte PAYLOAD_TYPE_DEVICE_INFO = 0;
    public final static byte PAYLOAD_TYPE_LOCATION = 1;

    private byte mType;
    private Parcelable mData;

    public MessagePayload(String id, String ip) {
        mType = PAYLOAD_TYPE_DEVICE_INFO;
        mData = new DeviceInfoMessage(id, ip);
    }

    public MessagePayload(Location location) {
        mType = PAYLOAD_TYPE_LOCATION;
        mData = new LocationMessage(location);
    }

    public byte getType() {
        return mType;
    }

    public Object getData() {
        return mData;
    }

    @Override
    public String toString() {
        return "Message{"
                + "type=" + mType
                + ", data=" + mData.toString()
                + '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByte(mType);
        dest.writeParcelable(mData, flags);
    }

    protected MessagePayload(Parcel in) {
        mType = in.readByte();
        switch (mType) {
            case PAYLOAD_TYPE_DEVICE_INFO:
                mData = in.readParcelable(DeviceInfoMessage.class.getClassLoader());
                break;
            case PAYLOAD_TYPE_LOCATION:
                mData = in.readParcelable(LocationMessage.class.getClassLoader());
                break;

        }
    }

    public static final Creator<MessagePayload> CREATOR = new Creator<MessagePayload>() {
        @Override
        public MessagePayload createFromParcel(Parcel in) {
            return new MessagePayload(in);
        }

        @Override
        public MessagePayload[] newArray(int size) {
            return new MessagePayload[size];
        }
    };

    /**
     * MessagePayload msg = new MessagePayload();
     * byte[] toByte = MessagePayload.marshall(msg);
     * ...
     * byte[] fromByte = cursor.getBlob(c);
     * MessagePayload msg = MessagePayload.unmarshall(fromByte, MessagePayload.CREATOR);
     */
    public static byte[] marshall(Parcelable parceable) {
        Parcel parcel = Parcel.obtain();
        parceable.writeToParcel(parcel, 0);
        byte[] bytes = parcel.marshall();
        parcel.recycle(); // not sure if needed or a good idea
        return bytes;
    }

    public static <T extends Parcelable> T unmarshall(byte[] bytes, Parcelable.Creator<T> creator) {
        Parcel parcel = unmarshall(bytes);
        return creator.createFromParcel(parcel);
    }

    public static Parcel unmarshall(byte[] bytes) {
        Parcel parcel = Parcel.obtain();
        parcel.unmarshall(bytes, 0, bytes.length);
        parcel.setDataPosition(0); // this is extremely important!
        return parcel;
    }
}