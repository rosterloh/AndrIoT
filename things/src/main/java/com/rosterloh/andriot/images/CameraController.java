package com.rosterloh.andriot.images;

import android.arch.lifecycle.MutableLiveData;
import android.content.Context;
import android.graphics.Bitmap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.google.firebase.crash.FirebaseCrash;
import com.rosterloh.andriot.sensors.DeskCamera;
import com.rosterloh.andriot.sensors.SensorHub;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CameraController implements ImageReader.OnImageAvailableListener {

    private static final String TAG = CameraController.class.getSimpleName();

    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 117;
    private static final float IMAGE_STD = 1;
    private static final String INPUT_NAME = "input";
    private static final String OUTPUT_NAME = "output";
    private static final String MODEL_FILE = "file:///android_asset/tensorflow_inception_graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/imagenet_comp_graph_label_strings.txt";

    private Context appContext;
    private DeskCamera camera;
    private Handler cameraHandler;
    private HandlerThread cameraThread;
    private AtomicBoolean ready = new AtomicBoolean(false);
    private ImagePreprocessor imagePreprocessor;
    private Classifier tensorFlowClassifier;

    public MutableLiveData<Bitmap> imageData = new MutableLiveData<>();

    public CameraController(Context context, SensorHub sensorHub) {

        appContext = context.getApplicationContext();

        cameraThread = new HandlerThread("CameraBackground");
        cameraThread.start();
        cameraHandler = new Handler(cameraThread.getLooper());
        cameraHandler.post(initialiseOnBackground);
    }

    private Runnable initialiseOnBackground = () -> {

        imagePreprocessor = new ImagePreprocessor(DeskCamera.IMAGE_WIDTH,
                DeskCamera.IMAGE_HEIGHT, INPUT_SIZE);

        camera = DeskCamera.getInstance();
        camera.initialiseCamera(appContext, cameraHandler, this);

        tensorFlowClassifier =
                TensorFlowImageClassifier.create(
                        appContext.getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);

        ready.set(true);
    };

    @Override
    public void onImageAvailable(ImageReader reader) {
        final Bitmap bitmap;
        try (Image image = reader.acquireNextImage()) {
            bitmap = imagePreprocessor.preprocessImage(image);
        }

        final List<Classifier.Recognition> results = tensorFlowClassifier.recognizeImage(bitmap);

        if (results.isEmpty()) {
            FirebaseCrash.log("Image not recognised");
        } else if (results.size() == 1 || results.get(0).getConfidence() > 0.4f) {
            FirebaseCrash.log("Found " + results.get(0).getTitle());
        } else {
            FirebaseCrash.log("Found either " + results.get(0).getTitle() + " or " + results.get(1).getTitle());
        }

        imageData.setValue(bitmap);

        ready.set(true);
        //setLedValue(false);
    }

    @Override
    protected void finalize() throws Throwable {

        try {
            if (cameraThread != null) cameraThread.quit();
        } catch (Throwable t) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error quitting camera thread");
            FirebaseCrash.report(t);
        }
        cameraThread = null;
        cameraHandler = null;

        try {
            if (camera != null) camera.shutDown();
        } catch (Throwable t) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error shutting down camera");
            FirebaseCrash.report(t);
        }

        try {
            if (tensorFlowClassifier != null) tensorFlowClassifier.close();
        } catch (Throwable t) {
            FirebaseCrash.logcat(Log.ERROR, TAG, "Error closing image classifier");
            FirebaseCrash.report(t);
        }

        super.finalize();
    }
}
