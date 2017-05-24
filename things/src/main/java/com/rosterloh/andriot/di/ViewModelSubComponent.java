package com.rosterloh.andriot.di;

import com.rosterloh.andriot.ui.dash.DashViewModel;

import dagger.Subcomponent;

/**
 * A sub component to create ViewModels. It is called by the
 * {@link com.rosterloh.andriot.viewmodel.ThingsViewModelFactory}. Using this component allows
 * ViewModels to define {@link javax.inject.Inject} constructors.
 */
@Subcomponent
public interface ViewModelSubComponent {
    @Subcomponent.Builder
    interface Builder {
        ViewModelSubComponent build();
    }
    DashViewModel dashViewModel();
}
