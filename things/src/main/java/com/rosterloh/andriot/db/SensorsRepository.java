package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.hardware.Sensor;

import com.rosterloh.andriot.AppExecutors;
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
    private final SensorHub mSensorHub;

    private LiveData<List<SensorData>> mSensorData;
    private MutableLiveData<SensorData> mCurrentData = new MutableLiveData<>();

    @Inject
    SensorsRepository(AppExecutors appExecutors, SensorDao sensorDao, SensorHub sensorHub) {
        mAppExecutors = appExecutors;
        mSensorDao = sensorDao;
        mSensorHub = sensorHub;

        mSensorData = mSensorDao.load();
        mSensorData.observeForever(data -> {
            if (data == null) {
                Timber.d("Empty data received");
                //mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new LocalSettings()));
            } else {
                if(data.size() > 0) {
                    Timber.d("New data " + data.get(data.size() - 1).toString());
                    mCurrentData.setValue(data.get(data.size() - 1));
                } else {
                    mCurrentData.setValue(new SensorData(new float[]{0, 0, 0 ,0}));
                }
                if (mSensorData.getValue().size() > 100) {
                    mAppExecutors.diskIO().execute(() -> mSensorDao.delete(mSensorData.getValue().get(0)));
                }
            }
        });

        LiveDataBus.subscribe(LiveDataBus.SUBJECT_MQTT_DATA, (data) -> {
            MqttEvent event = (MqttEvent) data;
            SensorData current = mCurrentData.getValue();
            switch (event.getTopic()) {
                case "/weather/co2":
                    //current.setECO2(Integer.parseInt(event.getMessage()));
                    mCurrentData.setValue(current);
                    break;
                case "/weather/tvoc":
                    //current.setTVOC(Integer.parseInt(event.getMessage()));
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
                appExecutors.mainThread().execute(() -> {
                    SensorData data = mSensorHub.getSensorData();
                    if (data != null) {
                        appExecutors.diskIO().execute(() -> mSensorDao.insert(data));
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

    public void setSensorValue(int type, float value) {
        SensorData current = mCurrentData.getValue();
        if (current != null) {
            switch (type) {
                case Sensor.TYPE_AMBIENT_TEMPERATURE:
                    current.setTemperature(value);
                    break;
                case Sensor.TYPE_RELATIVE_HUMIDITY:
                    current.setHumidity(value);
                    break;
                case Sensor.TYPE_PRESSURE:
                    current.setPressure(value);
                    break;
                case Sensor.TYPE_DEVICE_PRIVATE_BASE:
                    current.setAirQuality(value);
                    break;
            }
            mCurrentData.setValue(current);
        }
    }
}
