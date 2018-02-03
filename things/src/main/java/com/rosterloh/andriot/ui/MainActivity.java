package com.rosterloh.andriot.ui;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.ui.dash.DashFragment;
import com.rosterloh.andriot.ui.dash.GraphFragment;

import dagger.android.support.DaggerAppCompatActivity;
import timber.log.Timber;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

public class MainActivity extends DaggerAppCompatActivity {

    private static final int PERMISSIONS_REQUEST = 37;
    private static final int FRAGMENT_DASH = 0;
    private static final int FRAGMENT_TIMELINE = 1;
    private static final int FRAGMENT_SETTINGS = 2;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        setupSideBar();
        if (hasPermission()) {
            if (savedInstanceState == null) {
                navigateToFragment(FRAGMENT_DASH);
            }
        } else {
            requestPermission();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkPlayServices();
    }

    public void navigateToFragment(int id) {
        switch (id) {
            case FRAGMENT_DASH:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new DashFragment())
                        .commitAllowingStateLoss();
                break;
            case FRAGMENT_TIMELINE:
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container, new GraphFragment())
                        .commitAllowingStateLoss();
                break;
            case FRAGMENT_SETTINGS:
                break;
            default:
                Timber.e("Unknown navigation target " + id);
                break;
        }
    }

    public void setupSideBar() {
        SideBar sideBar = findViewById(R.id.sidebar);
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_home));
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_timeline));
        sideBar.addItem(sideBar.newItem().setIcon(R.drawable.ic_settings));

        sideBar.addOnItemSelectedListener(new SideBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(SideBar.Item item) {
                navigateToFragment(item.getPosition());
            }

            @Override
            public void onItemUnselected(SideBar.Item item) {

            }

            @Override
            public void onItemReselected(SideBar.Item item) {

            }
        });

    }

    public void checkPlayServices() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        //Crashlytics.log("PlayServices version " + api.GOOGLE_PLAY_SERVICES_VERSION_CODE);
        int result = api.isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            Timber.w("Google Play Services are not available");
            /*
            if(api.isUserResolvableError(result)) {
                api.getErrorDialog(this, result, GOOGLE_PLAY_SERVICES_REQUEST).show();
            }
            */
        }
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
                    navigateToFragment(FRAGMENT_DASH);
                } else {
                    requestPermission();
                }
                break;
            }
            default:
                Timber.w("Unknown permission request code " + requestCode);
        }
    }

    private boolean hasPermission() {
        return checkSelfPermission(CAMERA) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (shouldShowRequestPermissionRationale(CAMERA)
                || shouldShowRequestPermissionRationale(WRITE_EXTERNAL_STORAGE)
                || shouldShowRequestPermissionRationale(ACCESS_COARSE_LOCATION)) {
            Toast.makeText(MainActivity.this, "Camera, location AND storage permission are "
                    + "required for this application", Toast.LENGTH_LONG).show();
        }
        requestPermissions(new String[]{CAMERA, WRITE_EXTERNAL_STORAGE, ACCESS_COARSE_LOCATION}, PERMISSIONS_REQUEST);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
