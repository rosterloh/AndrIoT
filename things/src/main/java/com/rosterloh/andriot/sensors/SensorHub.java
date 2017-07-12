package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.crash.FirebaseCrash;
import com.rosterloh.things.driver.htu21d.Htu21d;

import java.io.IOException;

public class SensorHub {

    private static final String TAG = SensorHub.class.getSimpleName();

    private static final String LED_GPIO = "BCM27";
    private static final String BUTTON_GPIO = "BCM17";
    private static final String PIR_GPIO = "BCM21";

    private Gpio mLed;
    private Gpio mButton;
    private Gpio mPir;
    private Htu21d mHtu21d;

    private MutableLiveData<Boolean> mPirData = new MutableLiveData<>();
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
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error reading button state");
                FirebaseCrash.report(e);
            }

            return true;
        }
    };

    private GpioCallback mPirInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                mPirData.setValue(gpio.getValue());
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error reading PIR state");
                FirebaseCrash.report(e);
            }

            return true;
        }
    };

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

            mHtu21d = new Htu21d("I2C1");
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error configuring GPIO pins");
            FirebaseCrash.report(e);
        }
    }

    private void setLedValue(boolean value) {
        try {
            mLed.setValue(value);
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error updating GPIO value");
            FirebaseCrash.report(e);
        }
    }

    private boolean getLedValue() {
        try {
            return mLed.getValue();
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error getting GPIO value");
            FirebaseCrash.report(e);
            return false;
        }
    }

    public LiveData<Boolean> getPirData() {
        return mPirData;
    }

    public LiveData<Boolean> getButtonData() {
        return mButtonData;
    }

    public float[] getSensorData() {
        try {
            if (mHtu21d != null) {
                return mHtu21d.readTemperatureAndHumidity();
            }
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error reading sensor data");
            FirebaseCrash.report(e);
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
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing BUTTON GPIO");
                FirebaseCrash.report(e);
            } finally {
                mButton = null;
            }
        }

        if (mPir != null) {
            mPir.unregisterGpioCallback(mPirInterrupt);
            try {
                mPir.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing PIR GPIO");
                FirebaseCrash.report(e);
            } finally {
                mPir = null;
            }
        }

        if (mLed != null) {
            try {
                mLed.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing LED GPIO");
                FirebaseCrash.report(e);
            } finally {
                mLed = null;
            }
        }

        if (mHtu21d != null) {
            try {
                mHtu21d.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing HTU21D");
                FirebaseCrash.report(e);
            } finally {
                mHtu21d = null;
            }
        }

        super.finalize();
    }
}
