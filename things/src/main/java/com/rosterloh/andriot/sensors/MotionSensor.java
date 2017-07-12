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

    private Gpio mMotionDetectorGpio;
    private OnMotionDetectedEventListener mOnMotionDetectedEventListener;
    private boolean mLastState;

    private GpioCallback mInterruptCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue() != mLastState) {
                    mLastState = gpio.getValue();
                    performMotionEvent(mLastState ? State.STATE_HIGH : State.STATE_LOW);
                }
            } catch (IOException e) {

            }

            return true;
        }
    };

    public MotionSensor(String pin) throws IOException {

        PeripheralManagerService pioService = new PeripheralManagerService();

        Gpio gpio = pioService.openGpio(pin);

        try {
            connect(gpio);
        } catch (IOException e) {
            close();
            throw e;
        }
    }

    private void connect(Gpio gpio) throws IOException {

        mMotionDetectorGpio = gpio;
        mMotionDetectorGpio.setDirection(Gpio.DIRECTION_IN);
        mMotionDetectorGpio.setEdgeTriggerType(Gpio.EDGE_BOTH);

        mLastState = mMotionDetectorGpio.getValue();

        mMotionDetectorGpio.setActiveType(mLastState ? Gpio.ACTIVE_HIGH : Gpio.ACTIVE_LOW);

        mMotionDetectorGpio.registerGpioCallback(mInterruptCallback);
    }

    private void performMotionEvent(State state) {
        if (mOnMotionDetectedEventListener != null) {
            mOnMotionDetectedEventListener.onMotionDetectedEvent(state);
        }
    }

    public void setOnMotionDetectedEventListener(OnMotionDetectedEventListener listener) {
        mOnMotionDetectedEventListener = listener;
    }

    @Override
    public void close() throws IOException {
        mOnMotionDetectedEventListener = null;

        if (mMotionDetectorGpio != null) {
            mMotionDetectorGpio.unregisterGpioCallback(mInterruptCallback);
            try {
                mMotionDetectorGpio.close();
            } finally {
                mMotionDetectorGpio = null;
            }
        }
    }

    public interface OnMotionDetectedEventListener {
        void onMotionDetectedEvent(State state);
    }
}
