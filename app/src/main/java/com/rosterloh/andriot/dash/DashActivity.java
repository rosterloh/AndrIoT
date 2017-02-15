package com.rosterloh.andriot.dash;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.rosterloh.andriot.sensors.SensorHub;
import com.rosterloh.andriot.ui.ViewModelHolder;
import com.rosterloh.andriot.networking.MqttManager;
import com.rosterloh.andriot.sensors.DeskCamera;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.utils.ActivityUtils;

import java.nio.ByteBuffer;

public class DashActivity extends AppCompatActivity implements DashNavigator {

    private static final String TAG = DashActivity.class.getSimpleName();
    public static final String DASH_VIEWMODEL_TAG = "DASH_VIEWMODEL_TAG";
    private DashViewModel viewModel;

    private MqttManager mqttManager;
    private SensorHub sensorHub;

    private DeskCamera camera;
    private Handler cameraHandler;
    private HandlerThread cameraThread;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_act);

        DashFragment dashFragment = findOrCreateViewFragment();

        viewModel = findOrCreateViewModel();

        // Link View and ViewModel
        dashFragment.setViewModel(viewModel);

        setupCamera();

        mqttManager = MqttManager.getInstance(getApplicationContext());
        sensorHub = SensorHub.getInstance(getApplicationContext());
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

        sensorHub.destroyInstance();

        camera.shutDown();
        cameraThread.quitSafely();
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
