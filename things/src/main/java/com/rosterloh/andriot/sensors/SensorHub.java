package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.crashlytics.android.Crashlytics;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.knobtviker.android.things.contrib.driver.bme680.Bme680SensorDriver;

import java.io.IOException;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

@Singleton
public class SensorHub {

    private static final String TAG = SensorHub.class.getSimpleName();
    private static final String I2C_BUS = "I2C1";

    private static final String LED_GPIO = "BCM27";
    private static final String BUTTON_GPIO = "BCM17";
    private static final String PIR_GPIO = "BCM21";

    private Gpio mLed;
    private Gpio mButton;
    private Gpio mPir;

    private MutableLiveData<Boolean> mButtonData = new MutableLiveData<>();

    private GpioCallback mButtonInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                mButtonData.setValue(gpio.getValue());

                /*
                if (ready.get()) {
                    setLedValue(true);
                    ready.set(false);
                    camera.takePicture();
                }*/
            } catch (IOException e) {
                Crashlytics.logException(e);
            }

            return true;
        }
    };

    private GpioCallback mPirInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {

            try {
                LiveDataBus.publish(LiveDataBus.SUBJECT_MOTION_DATA, gpio.getValue());
            } catch (IOException e) {
                Crashlytics.logException(e);
            }

            return true;
        }
    };

    @Inject
    public SensorHub() {

        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            mLed = pioService.openGpio(LED_GPIO);
            mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mButton = pioService.openGpio(BUTTON_GPIO);
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButton.setActiveType(Gpio.ACTIVE_LOW);
            mButton.registerGpioCallback(mButtonInterrupt);

            mPir = pioService.openGpio(PIR_GPIO);
            mPir.setDirection(Gpio.DIRECTION_IN);
            mPir.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mPir.setActiveType(Gpio.ACTIVE_HIGH);
            mPir.registerGpioCallback(mPirInterrupt);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        try {
            final Bme680SensorDriver mBmeSensorDriver = new Bme680SensorDriver(I2C_BUS);
            mBmeSensorDriver.registerTemperatureSensor();
            mBmeSensorDriver.registerHumiditySensor();
            mBmeSensorDriver.registerPressureSensor();
            mBmeSensorDriver.registerGasSensor();
        } catch (IOException e) {
            Timber.w("BME680 failed to start or is not present for this device");
        }
    }

    private void setLedValue(boolean value) {
        try {
            mLed.setValue(value);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
    }

    private boolean getLedValue() {
        try {
            return mLed.getValue();
        } catch (IOException e) {
            Crashlytics.logException(e);
            return false;
        }
    }

    public LiveData<Boolean> getButtonData() {
        return mButtonData;
    }
/*
    public SensorData getSensorData() {
        try {
            float[] val1 = {0f, 0f};
            int[] val2 = {0 ,0, 0, 0};
            if (mHtu21d != null) {
                val1 = mHtu21d.readTemperatureAndHumidity();
            } else if(mBmx280 != null) {
                val1 = mBmx280.readTemperatureAndHumidity();
            }
            if (mCcs811 != null) {
                val2 = mCcs811.readAlgorithmResults();
                Timber.d("Status: " + val2[2] + " Error: " + val2[3]);

            }
            return new SensorData(val1, val2);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
        return null;
    }
*/
    @Override
    protected void finalize() throws Throwable {

        if (mButton != null) {
            mButton.unregisterGpioCallback(mButtonInterrupt);
            try {
                mButton.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            } finally {
                mButton = null;
            }
        }

        if (mPir != null) {
            mPir.unregisterGpioCallback(mPirInterrupt);
            try {
                mPir.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            } finally {
                mPir = null;
            }
        }

        if (mLed != null) {
            try {
                mLed.close();
            } catch (IOException e) {
                Crashlytics.logException(e);
            } finally {
                mLed = null;
            }
        }

        super.finalize();
    }
}
