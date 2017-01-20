package com.rosterloh.andriot.sensors;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class MotionSensor implements AutoCloseable {

    public enum State {
        STATE_HIGH,
        STATE_LOW
    }

    public interface OnMotionDetectedEventListener {
        void onMotionDetectedEvent(State state);
    }

    private Gpio motionDetectorGpio;
    private OnMotionDetectedEventListener onMotionDetectedEventListener;
    private boolean lastState;

    public MotionSensor(String pin) throws IOException {

        PeripheralManagerService pioService = new PeripheralManagerService();

        Gpio gpio = pioService.openGpio(pin);

        try {
            connect(gpio);
        } catch( IOException | RuntimeException e ) {
            close();
            throw e;
        }
    }

    private void connect(Gpio gpio) throws IOException {

        motionDetectorGpio = gpio;
        motionDetectorGpio.setDirection(Gpio.DIRECTION_IN);
        motionDetectorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);

        lastState = motionDetectorGpio.getValue();

        motionDetectorGpio.setActiveType(lastState ? Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW);

        motionDetectorGpio.registerGpioCallback(interruptCallback);
    }

    private void performMotionEvent(State state) {

        if( onMotionDetectedEventListener != null ) {
            onMotionDetectedEventListener.onMotionDetectedEvent(state);
        }
    }

    private GpioCallback interruptCallback = new GpioCallback() {

        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {

                if( gpio.getValue() != lastState ) {
                    lastState = gpio.getValue();
                    performMotionEvent(lastState ? State.STATE_HIGH : State.STATE_LOW);
                }


            } catch( IOException e ) {

            }

            return true;
        }
    };

    public void setOnMotionDetectedEventListener(OnMotionDetectedEventListener listener) {
        onMotionDetectedEventListener = listener;
    }

    @Override
    public void close() throws IOException {
        onMotionDetectedEventListener = null;

        if (motionDetectorGpio != null) {
            motionDetectorGpio.unregisterGpioCallback(interruptCallback);
            try {
                motionDetectorGpio.close();
            } finally {
                motionDetectorGpio = null;
            }
        }
    }
}
