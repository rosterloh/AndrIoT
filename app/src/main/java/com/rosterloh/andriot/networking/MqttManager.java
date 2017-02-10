package com.rosterloh.andriot.networking;

import android.content.Context;
import android.util.Log;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.android.service.MqttTraceHandler;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import io.reactivex.Observable;
import io.reactivex.subjects.PublishSubject;

public class MqttManager implements IMqttActionListener, MqttCallbackExtended, MqttTraceHandler {

    private static final String TAG = MqttManager.class.getSimpleName();
    private static MqttManager instance;

    private MqttAndroidClient client = null;
    private MqttConnectOptions connectOptions;
    private static final String serverUri = "tcp://192.168.86.77:1883";
    private static final String clientId = MqttClient.generateClientId();
    private static final String clientTopic = "office/rpi";
    private static final String lastWillTopic = "office/status";
    private ConnectionStatus status = ConnectionStatus.NONE;

    public enum ConnectionStatus {
        CONNECTING,
        CONNECTED,
        DISCONNECTING,
        DISCONNECTED,
        ERROR,
        NONE
    }

    private PublishSubject<String> mqttObservable = PublishSubject.create();

    private MqttManager(Context context) {

        client = new MqttAndroidClient(context, serverUri, clientId);
        client.registerResources(context);
        client.setCallback(this);
        client.setTraceCallback(this);

        connectOptions = new MqttConnectOptions();
        connectOptions.setAutomaticReconnect(true);
        connectOptions.setConnectionTimeout(MqttConnectOptions.CONNECTION_TIMEOUT_DEFAULT);
        connectOptions.setKeepAliveInterval(MqttConnectOptions.KEEP_ALIVE_INTERVAL_DEFAULT);
        connectOptions.setWill(lastWillTopic, "AndrIoT down".getBytes(), 0, false);

        try {
            status = ConnectionStatus.CONNECTING;
            client.connect(connectOptions, null, this);
        } catch (MqttException e) {
            Log.e(TAG, "Failed to connect to MQTT server");
        }
    }

    public static MqttManager getInstance(Context context) {

        synchronized (MqttManager.class) {
            if (instance == null) {
                instance = new MqttManager(context);
            }
            return instance;
        }
    }

    public Observable<String> observeMqtt() {
        return mqttObservable;
    }

    private void subscribe(String topic, int qos) {

        if (client != null && status == ConnectionStatus.CONNECTED && topic != null) {
            try {
                Log.d(TAG, "subscribe to " + topic + " qos:" + qos);
                client.subscribe(topic, qos);
            } catch (MqttException e) {
                Log.e(TAG, "subscribe error: ", e);
            }
        }
    }

    @Override
    public void onSuccess(IMqttToken asyncActionToken) {

        if (status == ConnectionStatus.CONNECTING) {
            status = ConnectionStatus.CONNECTED;
            subscribe(clientTopic, 0);
        } else if (status == ConnectionStatus.DISCONNECTING) {
            status = ConnectionStatus.DISCONNECTED;
        } else {
            Log.d(TAG, "unknown onSuccess");
        }
    }

    @Override
    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
        status = ConnectionStatus.ERROR;
    }

    @Override
    public void connectComplete(boolean reconnect, String serverURI) {
        if (reconnect) {
            Log.d(TAG, "Reconnected to : " + serverURI);
        } else {
            Log.d(TAG, "Connected to: " + serverURI);
            subscribe(clientTopic, 0);
        }
    }

    @Override
    public void connectionLost(Throwable cause) {
        Log.d(TAG, "connectionLost Cause: " + cause.getMessage());
    }

    @Override
    public void messageArrived(String topic, MqttMessage message) throws Exception {

        String payload = new String(message.getPayload());

        Log.d(TAG, "messageArrived from topic: " + topic + " message: " + payload + " qos: " + message.getQos());

        mqttObservable.onNext(payload);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken token) {
        Log.d(TAG, "deliveryComplete");
    }

    @Override
    public void traceDebug(String source, String message) {
        Log.d(TAG, "traceDebug: Source: " + source + " Message: " + message);
    }

    @Override
    public void traceError(String source, String message) {
        Log.d(TAG, "traceError: Source: " + source + " Message: " + message);
    }

    @Override
    public void traceException(String source, String message, Exception e) {
        Log.d(TAG, "traceException: Source: " + source + " Message: " + message);
    }
}
