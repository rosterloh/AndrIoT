package com.rosterloh.andriot.sensors;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class SensorHub {

    private static final String TAG = SensorHub.class.getSimpleName();
    private static SensorHub instance;

    private static final String LED_GPIO = "BCM27";
    private static final String BUTTON_GPIO = "BCM17";

    private SensorManager sensorManager;
    private Gpio led;
    private Gpio button;

    private SensorHub(Context context) {

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensors = sensorManager.getDynamicSensorList(Sensor.TYPE_ALL);
        for ( Sensor s : sensors) {
            Log.d(TAG, "Sensor " + s.getName() + " (" + s.getId() + ") " + " is " + s.getType());
        }

        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            led = pioService.openGpio(LED_GPIO);
            led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            button = pioService.openGpio(BUTTON_GPIO);
            button.setDirection(Gpio.DIRECTION_IN);
            button.setEdgeTriggerType(Gpio.EDGE_FALLING);
            button.setActiveType(Gpio.ACTIVE_LOW);
            button.registerGpioCallback(buttonInterrupt);
        } catch (IOException e) {
            Log.e(TAG, "Error configuring GPIO pins", e);
        }
    }

    public static SensorHub getInstance(Context context) {

        synchronized (SensorHub.class) {
            if (instance == null) {
                instance = new SensorHub(context);
            }
            return instance;
        }
    }

    private Observable<Integer> getButtonObservable() {
        return Observable.create((emitter) -> emitter.onComplete());
    }

    private Observer<Integer> getButtonObserver() {
        return new Observer<Integer>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(Integer integer) {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {
                setLedValue(!getLedValue());
            }
        };
    }

    private GpioCallback buttonInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (!gpio.getValue()) {
                    getButtonObservable()
                            .debounce(500, TimeUnit.MILLISECONDS)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(getButtonObserver());
                }
            } catch (IOException e) {
                Log.e(TAG, "Error reading button state", e);
            }

            return true;
        }
    };

    private void setLedValue(boolean value) {
        try {
            led.setValue(value);
        } catch (IOException e) {
            Log.e(TAG, "Error updating GPIO value", e);
        }
    }

    private boolean getLedValue() {
        try {
            return led.getValue();
        } catch (IOException e) {
            Log.e(TAG, "Error getting GPIO value", e);
            return false;
        }
    }

    public void destroyInstance() {

        if (button != null) {
            button.unregisterGpioCallback(buttonInterrupt);
            try {
                button.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing BUTTON GPIO", e);
            } finally {
                button = null;
            }
        }

        if (led != null) {
            try {
                led.close();
            } catch (IOException e) {
                Log.e(TAG, "Error closing LED GPIO", e);
            } finally{
                led = null;
            }
        }
    }
}
