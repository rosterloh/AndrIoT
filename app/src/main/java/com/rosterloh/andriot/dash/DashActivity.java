package com.rosterloh.andriot.dash;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.rosterloh.andriot.images.ImageDialog;
import com.rosterloh.andriot.sensors.SensorHub;
import com.rosterloh.andriot.ui.ViewModelHolder;
import com.rosterloh.andriot.networking.MqttManager;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.utils.ActivityUtils;

import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class DashActivity extends AppCompatActivity implements DashNavigator {

    private static final String TAG = DashActivity.class.getSimpleName();
    public static final String DASH_VIEWMODEL_TAG = "DASH_VIEWMODEL_TAG";
    private static final int PERMISSIONS_REQUEST = 1;
    private DashViewModel viewModel;

    private MqttManager mqttManager;
    private SensorHub sensorHub;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dash_act);

        DashFragment dashFragment = findOrCreateViewFragment();

        viewModel = findOrCreateViewModel();

        // Link View and ViewModel
        dashFragment.setViewModel(viewModel);

        if (hasPermission()) {
            if (savedInstanceState == null) {
                sensorHub = SensorHub.getInstance(getApplicationContext());
                sensorHub.observeImages()
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Observer<Bitmap>() {
                            @Override
                            public void onSubscribe(Disposable d) {

                            }

                            @Override
                            public void onNext(Bitmap bitmap) {
                                final ImageDialog dialog = ImageDialog.getInstance(bitmap);
                                dialog.show(getSupportFragmentManager(), null);
                            }

                            @Override
                            public void onError(Throwable e) {

                            }

                            @Override
                            public void onComplete() {

                            }
                        });
            }
        } else {
            requestPermission();
        }

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

        sensorHub.destroyInstance();
    }

    // Permission-related methods. This is not needed for Android Things, where permissions are
    // automatically granted. However, it is kept here in case the developer
    // needs to test on a regular Android device
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_REQUEST: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED
                        && grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                    sensorHub = SensorHub.getInstance(getApplicationContext());
                } else {
                    requestPermission();
                }
            }
        }
    }

    private boolean hasPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else {
            return true;
        }
    }

    private void requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (shouldShowRequestPermissionRationale(CAMERA) ||
                    shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE) ||
                    shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
                Toast.makeText(DashActivity.this, "Camera, location AND storage permission are " +
                        "required for this application", Toast.LENGTH_LONG).show();
            }
            requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
        }
    }
}
