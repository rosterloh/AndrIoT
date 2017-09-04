package com.rosterloh.andriot.sensors;

import android.support.annotation.NonNull;

public class MqttEvent {

    private final String mTopic;

    private final String mMessage;

    public MqttEvent(@NonNull String topic, String message) {
        mTopic = topic;
        mMessage = message;
    }

    public String getTopic() {
        return mTopic;
    }

    public String getMessage() {
        return mMessage;
    }
}
