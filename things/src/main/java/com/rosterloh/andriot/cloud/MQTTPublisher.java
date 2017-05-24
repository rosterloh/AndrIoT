package com.rosterloh.andriot.cloud;

import android.support.annotation.NonNull;
import android.util.Log;

import com.google.gson.Gson;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class MQTTPublisher implements AutoCloseable {

    private static final String TAG = MQTTPublisher.class.getSimpleName();

    // Indicate if this message should be a MQTT 'retained' message.
    private static final boolean SHOULD_RETAIN = false;

    // Use mqttQos=1 (at least once delivery), mqttQos=0 (at most once delivery) also supported.
    private static final int MQTT_QOS = 1;

    private MqttClient mqttClient;
    private AndrIotOptions andrIotOptions;
    private AtomicBoolean ready = new AtomicBoolean(false);

    public MQTTPublisher(@NonNull AndrIotOptions options) {

        if (!options.isValid()) {

        } else {
            //try {
                andrIotOptions = options;
            //    initialiseMqttClient();
            //} catch (MqttException | IOException | GeneralSecurityException e) {
            //    throw new IllegalArgumentException("Could not initialise MQTT", e);
            //}
        }
    }

    public void publish(List<SensorData> data) {/*
        try {
            if (isReady()) {
                if (mqttClient != null && !mqttClient.isConnected()) {
                    // if for some reason the mqtt client has disconnected, we should try to connect
                    // it again.
                    try {
                        initialiseMqttClient();
                    } catch (MqttException | IOException | GeneralSecurityException e) {
                        throw new IllegalArgumentException("Could not initialize MQTT", e);
                    }
                }
                String payload = new Gson().toJson(data); //MessagePayload.createMessagePayload(data);
                Log.d(TAG, "Publishing: " + payload);
                sendMessage(andrIotOptions.getTopicName(), payload.getBytes());
            }
        } catch (MqttException e) {
            throw new IllegalArgumentException("Could not send message", e);
        }*/
     }

     public boolean isReady() {
        return ready.get();
    }

    @Override
    public void close()/* throws MqttException */{
        andrIotOptions = null;/*
        if (mqttClient != null) {
            if (mqttClient.isConnected()) {
                mqttClient.disconnect();
            }
            mqttClient = null;
        }*/
    }
/*
    private void initialiseMqttClient()  {

        mqttClient = new MqttClient(andrIotOptions.getBrokerUrl(),
                andrIotOptions.getClientId(), new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setUserName(andrIotOptions.UNUSED_ACCOUNT_NAME);

        // generate the jwt password
        //options.setPassword(mqttAuth.createJwt(andrIotOptions.getProjectId()));

        mqttClient.connect(options);
        ready.set(true);
    }

    private void sendMessage(String mqttTopic, byte[] mqttMessage) throws MqttException {
        mqttClient.publish(mqttTopic, mqttMessage, MQTT_QOS, SHOULD_RETAIN);
    }*/
}
