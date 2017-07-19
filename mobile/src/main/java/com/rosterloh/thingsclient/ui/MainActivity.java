package com.rosterloh.thingsclient.ui;

import android.arch.lifecycle.LifecycleActivity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.ui.interact.InteractFragment;

import javax.inject.Inject;

import dagger.android.AndroidInjection;
import dagger.android.DispatchingAndroidInjector;
import dagger.android.support.HasSupportFragmentInjector;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class MainActivity extends LifecycleActivity implements HasSupportFragmentInjector {

    private final static int REQUEST_PERMISSION_REQ_CODE = 34;

    @Inject
    DispatchingAndroidInjector<Fragment> mDispatchingAndroidInjector;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        AndroidInjection.inject(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        if (hasRequiredPermissions()) {
            if (savedInstanceState == null) {
                navigateToInteract();
            }
        } else {
            requestRequiredPermissions();
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, final @NonNull String[] permissions, final @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_PERMISSION_REQ_CODE: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // We have been granted the Manifest.permission.ACCESS_FINE_LOCATION permission. Now we may proceed with scanning.
                    navigateToInteract();
                } else {
                    Toast.makeText(this, R.string.no_required_permission, Toast.LENGTH_SHORT).show();
                }
                break;
            }
        }
    }

    private boolean hasRequiredPermissions() {
        return checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestRequiredPermissions() {
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Toast.makeText(MainActivity.this, "Location permission is " +
                    "required for this application", Toast.LENGTH_LONG).show();
        }
        requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_PERMISSION_REQ_CODE);
    }

    private void navigateToInteract() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.container, new InteractFragment())
                .commit();
    }

    @Override
    public DispatchingAndroidInjector<Fragment> supportFragmentInjector() {
        return mDispatchingAndroidInjector;
    }
}
