package com.rosterloh.andriot.cloud;

import com.google.gson.Gson;
import com.rosterloh.andriot.db.SettingsDao;
import com.rosterloh.andriot.db.Settings;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class MQTTPublisher implements AutoCloseable {

    // Indicate if this message should be a MQTT 'retained' message.
    private static final boolean SHOULD_RETAIN = false;

    // Use mqttQos=1 (at least once delivery), mqttQos=0 (at most once delivery) also supported.
    private static final int MQTT_QOS = 1;

    private MqttAsyncClient mMqttClient = null;
    private final SettingsDao mSettingsDao;
    private Settings mSettings;
    private AtomicBoolean mReady = new AtomicBoolean(false);

    @Inject
    public MQTTPublisher(SettingsDao settingsDao) {

        mSettingsDao = settingsDao;

        initialiseSettings();

        try {
            initialiseMqttClient();
        } catch (Exception e) {
            Timber.d("Failed to initialise mqtt: " + e.getLocalizedMessage());
        }

    }

    public void publish(List<SensorData> data) {
        try {
            if (isReady()) {
                if (mMqttClient != null && !mMqttClient.isConnected()) {
                    // if for some reason the mqtt client has disconnected, we should try to connect
                    // it again.
                    try {
                        initialiseMqttClient();
                    } catch (MqttException e) {
                        throw new IllegalArgumentException("Could not initialize MQTT", e);
                    }
                }
                String payload = new Gson().toJson(data); //MessagePayload.createMessagePayload(data);
                Timber.d("Publishing: " + payload);
                sendMessage(mSettings.getTopicName(), payload.getBytes());
            }
        } catch (MqttException e) {
            throw new IllegalArgumentException("Could not send message", e);
        }
     }

     public boolean isReady() {
        return mReady.get();
    }

    @Override
    public void close() {
        if (mMqttClient != null) {
            try {
                mMqttClient.disconnect();
                if (mMqttClient.isConnected()) {
                    mMqttClient.close(); // maybe handle separately
                }
                mMqttClient = null;
            } catch (Exception e) {
                Timber.d("Failed to disconnect: " + e.getLocalizedMessage());
            }
        }
    }

    private void initialiseSettings() {

        mSettings = mSettingsDao.load().getValue();

        //if (settings.deviceId == null) {
        //    settings.deviceId = MqttAsyncClient.generateClientId();
        //}

        Timber.d(mSettings.toString());
    }

    private void initialiseMqttClient() throws MqttException, IllegalArgumentException {

        mMqttClient = new MqttAsyncClient(mSettings.getBrokerUrl(),
                mSettings.getClientId(), new MemoryPersistence());

        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1_1);
        options.setUserName(mSettings.UNUSED_ACCOUNT_NAME);

        // generate the jwt password
        //options.setPassword(mqttAuth.createJwt(settings.getProjectId()));

        mMqttClient.connect(options);
        mReady.set(true);
    }

    private void sendMessage(String mqttTopic, byte[] mqttMessage) throws MqttException {
        mMqttClient.publish(mqttTopic, mqttMessage, MQTT_QOS, SHOULD_RETAIN);
    }
}
