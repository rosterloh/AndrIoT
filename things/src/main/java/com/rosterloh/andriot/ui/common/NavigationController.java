package com.rosterloh.andriot.ui.common;

import android.support.v4.app.FragmentManager;

import com.rosterloh.andriot.MainActivity;
import com.rosterloh.andriot.R;
import com.rosterloh.andriot.ui.dash.DashFragment;

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

    public void navigateToDash() {
        DashFragment dashFragment = new DashFragment();
        fragmentManager.beginTransaction()
                .replace(containerId, dashFragment)
                .commitAllowingStateLoss();
    }
}
