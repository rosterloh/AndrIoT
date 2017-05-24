package com.rosterloh.thingsclient.ui.common;

import android.bluetooth.BluetoothDevice;
import android.support.v4.app.FragmentManager;

import com.rosterloh.thingsclient.MainActivity;
import com.rosterloh.thingsclient.R;
import com.rosterloh.thingsclient.ui.interact.InteractFragment;
import com.rosterloh.thingsclient.ui.scan.ScanFragment;

import javax.inject.Inject;

/**
 * A utility class that handles navigation in {@link MainActivity}.
 */
public class NavigationController {
    private final int containerId;
    private final FragmentManager fragmentManager;
    @Inject
    public NavigationController(MainActivity mainActivity) {
        this.containerId = R.id.container;
        this.fragmentManager = mainActivity.getSupportFragmentManager();
    }

    public void navigateToInteract(BluetoothDevice device) {
        InteractFragment interactFragment = InteractFragment.create(device);
        fragmentManager.beginTransaction()
                .replace(containerId, interactFragment)
                .commitAllowingStateLoss();
    }

    public void navigateToScan() {
        ScanFragment scanFragment = new ScanFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, scanFragment)
                .addToBackStack(null)
                .commitAllowingStateLoss();
    }
}
