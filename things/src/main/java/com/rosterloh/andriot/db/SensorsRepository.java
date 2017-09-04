package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.rosterloh.andriot.AppExecutors;
import com.rosterloh.andriot.cloud.MQTTPublisher;
import com.rosterloh.andriot.sensors.LiveDataBus;
import com.rosterloh.andriot.sensors.MqttEvent;
import com.rosterloh.andriot.sensors.SensorHub;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class SensorsRepository {

    private static final int POLL_RATE = 5 * 60 * 1000;
    private static final int INIT_DELAY = 5 * 1000;

    private final AppExecutors mAppExecutors;
    private final SensorDao mSensorDao;
    private final SensorManager mSensorManager;
    private final SensorHub mSensorHub;
    private final MQTTPublisher mMQTTPublisher;

    private LiveData<List<SensorData>> mSensorData;
    private int mECO2 = 0;
    private int mTVOC = 0;

    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Timber.d(sensorEvent.values.length + " new sensor values");
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    Timber.d("New temperature " + sensorEvent.values[0]);
                    break;
                case Sensor.TYPE_PRESSURE:
                    Timber.d("New pressure " + sensorEvent.values[0]);
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    Timber.d("New humidity " + sensorEvent.values[0]);
                    break;
                default:
                    Timber.w("Unknown sensor changed " + sensorEvent);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            Timber.d("Sensor accuracy changed to " + accuracy);
        }
    };

    @Inject
    SensorsRepository(AppExecutors appExecutors, SensorDao sensorDao, SensorManager sensorManager,
                      SensorHub sensorHub, MQTTPublisher mqttPublisher) {
        mAppExecutors = appExecutors;
        mSensorDao = sensorDao;
        mSensorManager = sensorManager;
        mSensorHub = sensorHub;
        mMQTTPublisher = mqttPublisher;

        mSensorData = mSensorDao.load();
        mSensorData.observeForever(data -> {
            if (data == null) {
                Timber.d("Empty data received");
                //mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new LocalSettings()));
            } else {
                Timber.d("New data " + data.toString());
                if (mSensorData.getValue().size() > 100) {
                    mAppExecutors.diskIO().execute(() ->
                        mSensorDao.delete(mSensorData.getValue().get(0)));
                }
            }
        });

        mSensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE
                        || sensor.getType() == Sensor.TYPE_PRESSURE
                        || sensor.getType() == Sensor.TYPE_RELATIVE_HUMIDITY) {
                    mSensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });

        LiveDataBus.subscribe(LiveDataBus.SUBJECT_MQTT_DATA, (data) -> {
            MqttEvent event = (MqttEvent) data;
            switch (event.getTopic()) {
                case "/weather/co2":
                    mECO2 = Integer.parseInt(event.getMessage());
                    break;
                case "/weather/tvoc":
                    mTVOC = Integer.parseInt(event.getMessage());
                    break;
                case "/weather/temperature":
                    break;
                default:
                    Timber.w("Unknown topic " + event.getTopic());
            }
        });

        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                appExecutors.diskIO().execute(() -> {
                    SensorData data = mSensorHub.getSensorData();
                    data.setECO2(mECO2);
                    data.setTVOC(mTVOC);
                    if (data != null) {
                        appExecutors.mainThread().execute(() -> mSensorData.getValue().add(data));
                    }
                });
            }
        }, INIT_DELAY, POLL_RATE);
    }

    public LiveData<List<SensorData>> loadValues() {
        return mSensorData;
    }
}
