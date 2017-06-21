package com.rosterloh.andriot.cloud;

import com.rosterloh.andriot.db.SettingsDao;
import com.rosterloh.andriot.vo.Settings;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;

import timber.log.Timber;

public class MQTTPublisher implements AutoCloseable {

    // Indicate if this message should be a MQTT 'retained' message.
    private static final boolean SHOULD_RETAIN = false;

    // Use mqttQos=1 (at least once delivery), mqttQos=0 (at most once delivery) also supported.
    private static final int MQTT_QOS = 1;

    private MqttAsyncClient mqttClient = null;
    private final SettingsDao settingsDao;
    private Settings settings;
    private AtomicBoolean ready = new AtomicBoolean(false);

    @Inject
    public MQTTPublisher(SettingsDao settingsDao) {

        this.settingsDao = settingsDao;

        initialiseSettings();

        initialiseMqttClient();
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
    public void close() {
        if (mqttClient != null) {
            try {
                mqttClient.disconnect();
                if (mqttClient.isConnected()) {
                    mqttClient.close(); // maybe handle separately
                }
                mqttClient = null;
            } catch (Exception e) {
                Timber.d("Failed to disconnect: " + e.getLocalizedMessage());
            }
        }
    }

    private void initialiseSettings() {

        settings = settingsDao.load().getValue();

        //if (settings.deviceId == null) {
        //    settings.deviceId = MqttAsyncClient.generateClientId();
        //}

        Timber.d(settings.toString());
    }

    private void initialiseMqttClient() {

        try {
            mqttClient = new MqttAsyncClient(settings.getBrokerUrl(),
                    settings.getClientId(), new MemoryPersistence());

            //MqttConnectOptions options = new MqttConnectOptions();
            //options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
            //options.setUserName(settings.UNUSED_ACCOUNT_NAME);

            // generate the jwt password
            //options.setPassword(mqttAuth.createJwt(settings.getProjectId()));

            mqttClient.connect(/*options*/);
            ready.set(true);
        } catch (Exception e) {
            Timber.d("Failed to initialise mqtt: " + e.getLocalizedMessage());
        }
    }

    private void sendMessage(String mqttTopic, byte[] mqttMessage) throws MqttException {
        mqttClient.publish(mqttTopic, mqttMessage, MQTT_QOS, SHOULD_RETAIN);
    }
}
