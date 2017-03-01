package com.rosterloh.andriot.sensors;

import android.content.Context;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManagerService;
import com.rosterloh.andriot.images.Classifier;
import com.rosterloh.andriot.images.ImagePreprocessor;
import com.rosterloh.andriot.images.TensorFlowImageClassifier;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subjects.PublishSubject;

public class SensorHub implements ImageReader.OnImageAvailableListener {

    private static final String TAG = SensorHub.class.getSimpleName();
    private static SensorHub instance;

    private static final String LED_GPIO = "BCM27";
    private static final String BUTTON_GPIO = "BCM17";

    private Context appContext;
    private SensorManager sensorManager;
    private Gpio led;
    private Gpio button;
    private DeskCamera camera;
    private Handler cameraHandler;
    private HandlerThread cameraThread;
    private AtomicBoolean ready = new AtomicBoolean(false);
    private ImagePreprocessor imagePreprocessor;
    private TensorFlowImageClassifier tensorFlowClassifier;
    private LocationService locationService;

    private PublishSubject<Boolean> buttonObservable = PublishSubject.create();
    private PublishSubject<Bitmap> imageObservable = PublishSubject.create();

    private SensorHub(Context context) {

        appContext = context.getApplicationContext();
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

        List<Sensor> sensors = sensorManager.getDynamicSensorList(Sensor.TYPE_ALL);
        for ( Sensor s : sensors) {
            Log.d(TAG, "Sensor " + s.getName() + " (" + s.getId() + ") " + " is " + s.getType());
        }

        buttonObservable.debounce(500, TimeUnit.MILLISECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(getButtonObserver());

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

        cameraThread = new HandlerThread("CameraBackground");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
        cameraHandler.post(initialiseOnBackground);

        locationService = new LocationService(appContext);
        locationService.getLastLocation();
    }

    public static SensorHub getInstance(Context context) {

        synchronized (SensorHub.class) {
            if (instance == null) {
                instance = new SensorHub(context);
            }
            return instance;
        }
    }

    private Runnable initialiseOnBackground = () -> {

        imagePreprocessor = new ImagePreprocessor(DeskCamera.IMAGE_WIDTH,
                DeskCamera.IMAGE_HEIGHT, TensorFlowImageClassifier.INPUT_SIZE);

        camera = DeskCamera.getInstance();
        camera.initialiseCamera(appContext, cameraHandler, this);

        tensorFlowClassifier = new TensorFlowImageClassifier(appContext);

        ready.set(true);
    };

    public Observable<Bitmap> observeImages() {
        return imageObservable;
    }

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Bitmap bitmap;
        try (Image image = reader.acquireNextImage()) {
            bitmap = imagePreprocessor.preprocessImage(image);
        }

        final List<Classifier.Recognition> results = tensorFlowClassifier.recogniseImage(bitmap);

        if (results.isEmpty()) {
            Log.i(TAG, "Image not recognised");
        } else if (results.size() == 1 || results.get(0).getConfidence() > 0.4f) {
            Log.i(TAG, "Found " + results.get(0).getTitle());
        } else {
            Log.i(TAG, "Found either " + results.get(0).getTitle() + " or " + results.get(1).getTitle());
        }

        imageObservable.onNext(bitmap);

        ready.set(true);
        setLedValue(false);
    }

    private Observer<Boolean> getButtonObserver() {
        return new Observer<Boolean>() {
            @Override
            public void onSubscribe(Disposable d) {
                Log.d(TAG, "Subscribed to button events " + d.isDisposed());
            }

            @Override
            public void onNext(Boolean value) {
                Log.d(TAG, "Button is " + (value ? "HIGH" : "LOW"));
                if (ready.get()) {
                    setLedValue(true);
                    ready.set(false);
                    camera.takePicture();
                }
            }

            @Override
            public void onError(Throwable e) {
                Log.e(TAG, "Button error " + e.getMessage());
            }

            @Override
            public void onComplete() {
                Log.d(TAG, "Button observer complete");
            }
        };
    }

    private GpioCallback buttonInterrupt = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            try {
                buttonObservable.onNext(gpio.getValue());
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
            buttonObservable.onComplete();
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

        try {
            if (cameraThread != null) cameraThread.quit();
        } catch (Throwable t) {
            // close quietly
        }
        cameraThread = null;
        cameraHandler = null;

        try {
            if (camera != null) camera.shutDown();
        } catch (Throwable t) {
            // close quietly
        }

        try {
            if (tensorFlowClassifier != null) tensorFlowClassifier.close();
        } catch (Throwable t) {
            // close quietly
        }
    }
}
