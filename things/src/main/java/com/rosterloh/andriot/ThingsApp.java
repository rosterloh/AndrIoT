package com.rosterloh.andriot;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.annotation.NonNull;

import com.google.android.things.AndroidThings;
import com.knobtviker.android.things.contrib.driver.bme680.Bme680;
import com.knobtviker.android.things.contrib.driver.bme680.Bme680SensorDriver;
import com.rosterloh.andriot.db.SensorsRepository;
import com.rosterloh.andriot.di.AppComponent;
import com.rosterloh.andriot.di.DaggerAppComponent;

import javax.inject.Inject;

import dagger.android.AndroidInjector;
import dagger.android.support.DaggerApplication;
import timber.log.Timber;

public class ThingsApp extends DaggerApplication implements SensorEventListener {

    @Inject
    SensorsRepository sensorsRepository;

    @Override
    protected AndroidInjector<? extends DaggerApplication> applicationInjector() {
        AppComponent appComponent = DaggerAppComponent.builder().application(this).build();
        appComponent.inject(this);
        return appComponent;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }

        Timber.i("Built with Things Version: " + AndroidThings.getVersionString());

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            sensorManager.registerDynamicSensorCallback(new SensorManager.DynamicSensorCallback() {
                @Override
                public void onDynamicSensorConnected(Sensor sensor) {
                    registerListener(sensorManager, sensor);
                }
            });
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (sensorEvent.values.length > 1)
            Timber.d(sensorEvent.values.length + " new sensor values");

        int type = sensorEvent.sensor.getType();
        switch (type) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
                Timber.d("New temperature " + sensorEvent.values[0]);
                sensorsRepository.setSensorValue(type, sensorEvent.values[0]);
                break;
            case Sensor.TYPE_PRESSURE:
                Timber.d("New pressure " + sensorEvent.values[0]);
                sensorsRepository.setSensorValue(type, sensorEvent.values[0]);
                break;
            case Sensor.TYPE_RELATIVE_HUMIDITY:
                Timber.d("New humidity " + sensorEvent.values[0]);
                sensorsRepository.setSensorValue(type, sensorEvent.values[0]);
                break;
            case Sensor.TYPE_DEVICE_PRIVATE_BASE:
                if (sensorEvent.sensor.getStringType().equals(Bme680.CHIP_SENSOR_TYPE_IAQ)) {
                    Timber.d("New air quality " + sensorEvent.values[Bme680SensorDriver.INDOOR_AIR_QUALITY_INDEX]);
                    sensorsRepository.setSensorValue(type, sensorEvent.values[Bme680SensorDriver.INDOOR_AIR_QUALITY_INDEX]);
                }
                //IAQ classification and color-coding
                //  0 - 50 - good - #00e400
                //  51 - 100 - average - #ffff00
                //  101 - 200 - little bad - #ff7e00
                //  201 - 300 - bad - #ff0000
                //  301 - 400 - worse - #99004c
                //  401 - 500 - very bad - #000000
                break;
            default:
                Timber.w("Unknown sensor changed " + sensorEvent);
                break;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Timber.d(sensor.getName() + " accuracy changed to " + accuracy);
    }

    private void registerListener(@NonNull final SensorManager sensorManager, @NonNull final Sensor sensor) {
        switch (sensor.getType()) {
            case Sensor.TYPE_AMBIENT_TEMPERATURE:
            case Sensor.TYPE_RELATIVE_HUMIDITY:
            //case Sensor.TYPE_PRESSURE:
            case Sensor.TYPE_LIGHT:
                sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                break;
            case Sensor.TYPE_DEVICE_PRIVATE_BASE:
                if (sensor.getStringType().equals(Bme680.CHIP_SENSOR_TYPE_IAQ)) {
                    sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_NORMAL);
                }
                break;
        }
    }
}
