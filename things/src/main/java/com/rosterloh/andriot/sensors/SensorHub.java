package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.crashlytics.android.Crashlytics;
import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;
import com.knobtviker.android.things.contrib.community.driver.bme680.Bme680;
import com.rosterloh.andriot.db.SensorData;

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
    private Bme680 mBme680;

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
        final PeripheralManager peripheralManager = PeripheralManager.getInstance();
        try {
            mLed = peripheralManager.openGpio(LED_GPIO);
            mLed.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            mButton = peripheralManager.openGpio(BUTTON_GPIO);
            mButton.setDirection(Gpio.DIRECTION_IN);
            mButton.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButton.setActiveType(Gpio.ACTIVE_LOW);
            mButton.registerGpioCallback(mButtonInterrupt);

            mPir = peripheralManager.openGpio(PIR_GPIO);
            mPir.setDirection(Gpio.DIRECTION_IN);
            mPir.setEdgeTriggerType(Gpio.EDGE_BOTH);
            mPir.setActiveType(Gpio.ACTIVE_HIGH);
            mPir.registerGpioCallback(mPirInterrupt);
        } catch (IOException e) {
            Crashlytics.logException(e);
        }

        try {/*
            final Bme680SensorDriver mBmeSensorDriver = new Bme680SensorDriver(I2C_BUS);
            mBmeSensorDriver.registerTemperatureSensor();
            mBmeSensorDriver.registerPressureSensor();
            mBmeSensorDriver.registerHumiditySensor();
            mBmeSensorDriver.registerGasSensor();*/
            mBme680 = new Bme680(I2C_BUS);
            mBme680.setTemperatureOversample(Bme680.OVERSAMPLING_8X);
            mBme680.setPressureOversample(Bme680.OVERSAMPLING_4X);
            mBme680.setHumidityOversample(Bme680.OVERSAMPLING_2X);
            mBme680.setFilter(3);
            mBme680.setGasStatus(Bme680.ENABLE_GAS);
            mBme680.setGasHeaterProfile(Bme680.PROFILE_0, 320, 150);
            mBme680.selectGasHeaterProfile(Bme680.PROFILE_0);
            Timber.d(getSensorData().toString());
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

    public SensorData getSensorData() {
        try {
            float[] val = {0f, 0f, 0f, 0f};
            if (mBme680 != null) {
                val[0] = mBme680.readTemperature();
                val[1] = mBme680.readHumidity();
                val[2] = mBme680.readPressure();
                val[3] = mBme680.readAirQuality();
                return new SensorData(val);
            }
        } catch (IOException e) {
            Crashlytics.logException(e);
        }
        return null;
    }

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
