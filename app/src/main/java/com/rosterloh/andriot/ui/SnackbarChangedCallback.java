package com.rosterloh.andriot.ui;

import android.databinding.Observable;
import android.support.design.widget.Snackbar;
import android.view.View;

import java.lang.ref.WeakReference;

/**
 * Callback to apply to a {@link android.databinding.ObservableField<String>} that shows a Snackbar
 * whenever the text is updated.
 */
public class SnackbarChangedCallback extends Observable.OnPropertyChangedCallback {

    private final WeakReference<View> mView;

    private final SnackBarViewModel mViewModel;

    public SnackbarChangedCallback(View descendantOfCoordinatorLayout,
                                   SnackBarViewModel viewModel) {
        mView = new WeakReference<>(descendantOfCoordinatorLayout);
        mViewModel = viewModel;
    }

    @Override
    public void onPropertyChanged(Observable observable, int i) {
        if (mView.get() == null) {
            return;
        }
        Snackbar.make(mView.get(),
                mViewModel.getSnackbarText(),
                Snackbar.LENGTH_SHORT).show();
    }

    public interface SnackBarViewModel {
        String getSnackbarText();
    }
}
