package com.rosterloh.andriot.dash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rosterloh.andriot.ui.ViewModelHolder;
import com.rosterloh.andriot.networking.MqttManager;
import com.rosterloh.andriot.sensors.DeskCamera;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.utils.ActivityUtils;

import java.nio.ByteBuffer;
import java.util.List;

public class DashActivity extends AppCompatActivity implements DashNavigator {

    private static final String TAG = DashActivity.class.getSimpleName();
    public static final String DASH_VIEWMODEL_TAG = "DASH_VIEWMODEL_TAG";
    private DashViewModel viewModel;

    private MqttManager mqttManager;
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

        DashFragment dashFragment = findOrCreateViewFragment();

        viewModel = findOrCreateViewModel();

        // Link View and ViewModel
        dashFragment.setViewModel(viewModel);

        setupSensors();
        setupCamera();

        mqttManager = MqttManager.getInstance(getApplicationContext());
    }

    private DashViewModel findOrCreateViewModel() {
        // In a configuration change we might have a ViewModel present. It's retained using the
        // Fragment Manager.
        @SuppressWarnings("unchecked")
        ViewModelHolder<DashViewModel> retainedViewModel =
                (ViewModelHolder<DashViewModel>) getSupportFragmentManager()
                        .findFragmentByTag(DASH_VIEWMODEL_TAG);

        if (retainedViewModel != null && retainedViewModel.getViewmodel() != null) {
            // If the model was retained, return it.
            return retainedViewModel.getViewmodel();
        } else {
            // There is no ViewModel yet, create it.
            DashViewModel viewModel = new DashViewModel(getApplicationContext(), this);
            // and bind it to this Activity's lifecycle using the Fragment Manager.
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(),
                    ViewModelHolder.createContainer(viewModel),
                    DASH_VIEWMODEL_TAG);
            return viewModel;
        }
    }

    @NonNull
    private DashFragment findOrCreateViewFragment() {

        DashFragment fragment =
                (DashFragment) getSupportFragmentManager().findFragmentById(R.id.contentFrame);
        if (fragment == null) {
            // Create the fragment
            fragment = DashFragment.newInstance();
            ActivityUtils.addFragmentToActivity(
                    getSupportFragmentManager(), fragment, R.id.contentFrame);
        }
        return fragment;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        viewModel.handleActivityResult(requestCode, resultCode);
    }

    @Override
    public void showForecast() {
        viewModel.showForecast();
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

    private ImageReader.OnImageAvailableListener onImageAvailableListener = (reader) -> {
        Image image = reader.acquireLatestImage();

        // get image bytes
        ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
        final byte[] imageBytes = new byte[imageBuf.remaining()];
        imageBuf.get(imageBytes);
        image.close();

        //binding.ivCamera.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
    };
}
