package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.rosterloh.andriot.AppExecutors;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class SensorsRepository {

    private final AppExecutors mAppExecutors;
    private final SensorDao mSensorDao;
    private final SensorManager mSensorManager;

    private LiveData<List<SensorData>> mSensorData;

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
    SensorsRepository(AppExecutors appExecutors, SensorDao sensorDao, SensorManager sensorManager) {
        mAppExecutors = appExecutors;
        mSensorDao = sensorDao;
        mSensorManager = sensorManager;

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
                    mSensorManager.registerListener(mListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }
        });
    }
}
