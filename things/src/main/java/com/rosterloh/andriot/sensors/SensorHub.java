package com.rosterloh.andriot.sensors;

import android.arch.lifecycle.MutableLiveData;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.google.firebase.crash.FirebaseCrash;

import java.io.IOException;

public class SensorHub {

    private static final String TAG = SensorHub.class.getSimpleName();

    private static final String LED_GPIO = "BCM27";
    private static final String BUTTON_GPIO = "BCM17";
    private static final String PIR_GPIO = "BCM21";

    private Gpio led;
    private Gpio button;
    private Gpio pir;
    private HTU21D htu21d;

    public MutableLiveData<Boolean> pirData = new MutableLiveData<>();
    public MutableLiveData<Boolean> buttonData = new MutableLiveData<>();

    public SensorHub() {

        PeripheralManagerService pioService = new PeripheralManagerService();
        try {
            led = pioService.openGpio(LED_GPIO);
            led.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW);

            button = pioService.openGpio(BUTTON_GPIO);
            button.setDirection(Gpio.DIRECTION_IN);
            button.setEdgeTriggerType(Gpio.EDGE_FALLING);
            button.setActiveType(Gpio.ACTIVE_LOW);
            button.registerGpioCallback(buttonInterrupt);

            pir = pioService.openGpio(PIR_GPIO);
            pir.setDirection(Gpio.DIRECTION_IN);
            pir.setEdgeTriggerType(Gpio.EDGE_BOTH);
            pir.setActiveType(Gpio.ACTIVE_HIGH);
            pir.registerGpioCallback(pirInterrupt);

            htu21d = new HTU21D("I2C1");
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error configuring GPIO pins");
            FirebaseCrash.report(e);
        }
    }

    private GpioCallback buttonInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                buttonData.setValue(gpio.getValue());
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

    private GpioCallback pirInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                pirData.setValue(gpio.getValue());
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error reading PIR state");
                FirebaseCrash.report(e);
            }

            return true;
        }
    };

    private void setLedValue(boolean value) {
        try {
            led.setValue(value);
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error updating GPIO value");
            FirebaseCrash.report(e);
        }
    }

    private boolean getLedValue() {
        try {
            return led.getValue();
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error getting GPIO value");
            FirebaseCrash.report(e);
            return false;
        }
    }

    public float[] getSensorData() {
        try {
            if (htu21d != null) {
                return htu21d.readTemperatureAndHumidity();
            }
        } catch (IOException e) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error reading sensor data");
            FirebaseCrash.report(e);
        }
        return new float[]{0, 0};
    }

    @Override
    protected void finalize() throws Throwable {

        if (button != null) {
            button.unregisterGpioCallback(buttonInterrupt);
            try {
                button.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing BUTTON GPIO");
                FirebaseCrash.report(e);
            } finally {
                button = null;
            }
        }

        if (pir != null) {
            pir.unregisterGpioCallback(pirInterrupt);
            try {
                pir.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing PIR GPIO");
                FirebaseCrash.report(e);
            } finally {
                pir = null;
            }
        }

        if (led != null) {
            try {
                led.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing LED GPIO");
                FirebaseCrash.report(e);
            } finally{
                led = null;
            }
        }

        if (htu21d != null) {
            try {
                htu21d.close();
            } catch (IOException e) {
                FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing HTU21D");
                FirebaseCrash.report(e);
            } finally{
                htu21d = null;
            }
        }

        super.finalize();
    }
}
