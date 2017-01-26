package com.rosterloh.andriot.dash;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rosterloh.andriot.sensors.DeskCamera;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.data.DataRepository;
import com.rosterloh.andriot.utils.ActivityUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class DashActivity extends AppCompatActivity {

    private static final String TAG = DashActivity.class.getSimpleName();
    DashPresenter dashPresenter;

    private SensorManager sensorManager;
    //private Lsm9Ds1SensorDriver sensorDriver;
    //private float lastTemperature;

    private DeskCamera camera;
    private Handler cameraHandler;
    private HandlerThread cameraThread;

    /*
        // Callback used when we register the LSM9DS1 sensor driver with the system's SensorManager.
        private SensorManager.DynamicSensorCallback dynamicSensorCallback
                = new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    // Our sensor is connected. Start receiving temperature data.
                    sensorManager.registerListener(temperatureListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }

            @Override
            public void onDynamicSensorDisconnected(Sensor sensor) {
                super.onDynamicSensorDisconnected(sensor);
            }
        };

        // Callback when SensorManager delivers temperature data.
        private SensorEventListener temperatureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                lastTemperature = event.values[0];
                Log.d(TAG, "sensor changed: " + lastTemperature);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, "accuracy changed: " + accuracy);
            }
        };
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_act);

        DashFragment dashFragment =
                (DashFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (dashFragment == null) {
            // Create the fragment
            dashFragment = DashFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), dashFragment, R.id.contentFrame);
        }

        // Create the presenter
        dashPresenter = new DashPresenter(DataRepository.getInstance(getApplicationContext()), dashFragment);

        DashViewModel dashViewModel =
                new DashViewModel(getApplicationContext(), dashPresenter);

        dashFragment.setViewModel(dashViewModel);

        setupSensors();
        setupCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        /*
        // Clean up sensor registrations
        sensorManager.unregisterListener(temperatureListener);
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback);

        // Clean up peripheral.
        if (sensorDriver != null) {
            try {
                sensorDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sensorDriver = null;
        }*/

        camera.shutDown();
        cameraThread.quitSafely();
    }

    void setupSensors() {

        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        List<Sensor> sensors = sensorManager.getDynamicSensorList(Sensor.TYPE_ALL);
        for ( Sensor s : sensors) {
            Log.d(TAG, "Sensor " + s.getName() + " (" + s.getId() + ") " + " is " + s.getType());
        }

        /*
        try {
            sensorDriver = new Lsm9Ds1SensorDriver("I2C1");
            sensorManager.registerDynamicSensorCallback(dynamicSensorCallback);
            sensorDriver.registerTemperatureSensor();
            Log.d(TAG, "Initialised I2C LSM9DS1");
        } catch (IOException e) {
            throw new RuntimeException("Error initialising LSM9DS1"+ e);
        }*/
    }

    void setupCamera() {

        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d(TAG, "No camera permission");
        } else {
            cameraThread = new HandlerThread("CameraBackground");
            cameraThread.start();
            cameraHandler = new Handler(cameraThread.getLooper());

            camera = DeskCamera.getInstance();
            camera.initialiseCamera(this, cameraHandler, onImageAvailableListener);
        }
    }

    private ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();

            // get image bytes
            ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
            final byte[] imageBytes = new byte[imageBuf.remaining()];
            imageBuf.get(imageBytes);
            image.close();

            //binding.ivCamera.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
        }
    };
}
