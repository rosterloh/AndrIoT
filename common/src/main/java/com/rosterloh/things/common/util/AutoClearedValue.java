package com.rosterloh.things.common.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

/**
 * A value holder that automatically clears the reference if the Fragment's view is destroyed.
 * @param <T>
 */
public class AutoClearedValue<T> {

    private T value;

    public AutoClearedValue(Fragment fragment, T value) {
        FragmentManager fragmentManager = fragment.getFragmentManager();
        fragmentManager.registerFragmentLifecycleCallbacks(
                new FragmentManager.FragmentLifecycleCallbacks() {
                    @Override
                    public void onFragmentViewDestroyed(FragmentManager fm, Fragment f) {
                        AutoClearedValue.this.value = null;
                        fragmentManager.unregisterFragmentLifecycleCallbacks(this);
                    }
                },false);
        this.value = value;
    }

    public T get() {
        return value;
    }
}
