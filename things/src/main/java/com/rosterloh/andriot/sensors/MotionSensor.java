package com.rosterloh.andriot.sensors;

import android.support.annotation.IntDef;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public class MotionSensor implements AutoCloseable {

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({STATE_LOW, STATE_HIGH})
    public @interface State {}
    private static final int STATE_LOW = 0;
    private static final int STATE_HIGH = 1;

    private Gpio mMotionDetectorGpio;
    private OnMotionDetectedEventListener mOnMotionDetectedEventListener;
    private boolean mLastState;

    private GpioCallback mInterruptCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                if (gpio.getValue() != mLastState) {
                    mLastState = gpio.getValue();
                    performMotionEvent(mLastState ? STATE_HIGH : STATE_LOW);
                }
            } catch (IOException e) {

            }

            return true;
        }
    };

    public MotionSensor(String pin) throws IOException {

        final PeripheralManager peripheralManager = PeripheralManager.getInstance();
        Gpio gpio = peripheralManager.openGpio(pin);

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

    private void performMotionEvent(@State int state) {
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
        void onMotionDetectedEvent(@State int state);
    }
}
