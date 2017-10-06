package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
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
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class SensorsRepository {

    private static final long POLL_RATE = TimeUnit.MINUTES.toMillis(5);
    private static final long INIT_DELAY = TimeUnit.SECONDS.toMillis(5);

    private final AppExecutors mAppExecutors;
    private final SensorDao mSensorDao;
    private final SensorManager mSensorManager;
    private final SensorHub mSensorHub;
    private final MQTTPublisher mMQTTPublisher;
    private final FirebaseAdapter mFirebase;

    private LiveData<List<SensorData>> mSensorData;
    private MutableLiveData<SensorData> mCurrentData = new MutableLiveData<>();

    private SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            Timber.d(sensorEvent.values.length + " new sensor values");
            SensorData current = mCurrentData.getValue();
            switch (sensorEvent.sensor.getType()) {
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    Timber.d("New temperature " + sensorEvent.values[0]);
                    current.setTemperature(sensorEvent.values[0]);
                    mCurrentData.setValue(current);
                    break;
                case Sensor.TYPE_PRESSURE:
                    Timber.d("New pressure " + sensorEvent.values[0]);
                    current.setPressure(sensorEvent.values[0]);
                    mCurrentData.setValue(current);
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    Timber.d("New humidity " + sensorEvent.values[0]);
                    current.setHumidity(sensorEvent.values[0]);
                    mCurrentData.setValue(current);
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
                      SensorHub sensorHub, MQTTPublisher mqttPublisher, FirebaseAdapter firebaseAdapter) {
        mAppExecutors = appExecutors;
        mSensorDao = sensorDao;
        mSensorManager = sensorManager;
        mSensorHub = sensorHub;
        mMQTTPublisher = mqttPublisher;
        mFirebase = firebaseAdapter;

        mSensorData = mSensorDao.load();
        mSensorData.observeForever(data -> {
            if (data == null) {
                Timber.d("Empty data received");
                //mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new LocalSettings()));
            } else {
                Timber.d("New data " + data.toString());
                if(data.size() > 0) {
                    mCurrentData.setValue(data.get(data.size() - 1));
                }
                if (mSensorData.getValue().size() > 100) {
                    mAppExecutors.diskIO().execute(() -> mSensorDao.delete(mSensorData.getValue().get(0)));
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
            SensorData current = mCurrentData.getValue();
            switch (event.getTopic()) {
                case "/weather/co2":
                    current.setECO2(Integer.parseInt(event.getMessage()));
                    mCurrentData.setValue(current);
                    break;
                case "/weather/tvoc":
                    current.setTVOC(Integer.parseInt(event.getMessage()));
                    mCurrentData.setValue(current);
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
                    if (data != null) {
                        SensorData current = mCurrentData.getValue();
                        if (current != null) {
                            data.setECO2(current.getECO2());
                            data.setTVOC(current.getTVOC());
                        }
                        mSensorDao.insert(data);

                        //mFirebase.uploadSensorData(data);
                    }
                });
            }
        }, INIT_DELAY, POLL_RATE);
    }

    public LiveData<List<SensorData>> loadValues() {
        return mSensorData;
    }

    public LiveData<SensorData> getCurrentValue() {
        return mCurrentData;
    }
}
