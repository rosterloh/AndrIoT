package com.rosterloh.thingsclient.di;

import com.rosterloh.thingsclient.ui.interact.InteractViewModel;
import com.rosterloh.thingsclient.ui.scan.ScanViewModel;

import dagger.Subcomponent;

/**
 * A sub component to create ViewModels. It is called by the
 * {@link com.rosterloh.thingsclient.viewmodel.ThingsViewModelFactory}. Using this component allows
 * ViewModels to define {@link javax.inject.Inject} constructors.
 */
@Subcomponent
public interface ViewModelSubComponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubComponent build();
    }
    InteractViewModel interactViewModel();
    ScanViewModel scanViewModel();
}
