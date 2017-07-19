package com.rosterloh.andriot.db;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.rosterloh.andriot.AppExecutors;

import javax.inject.Inject;
import javax.inject.Singleton;

import timber.log.Timber;

/**
 * Repository that handles application settings.
 */
@Singleton
public class SettingsRepository {

    private final AppExecutors mAppExecutors;
    private final SettingsDao mSettingsDao;

    private LiveData<LocalSettings> mLocalSettings;
    private LiveData<CloudSettings> mCloudSettings;

    @Inject
    SettingsRepository(AppExecutors appExecutors, SettingsDao settingsDao) {
        mAppExecutors = appExecutors;
        mSettingsDao = settingsDao;

        mLocalSettings = mSettingsDao.loadLocalSettings();
        if (mLocalSettings.getValue() == null) {
            Timber.d("Creating default local settings");
            mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new LocalSettings()));
        } else {
            Timber.d(mLocalSettings.getValue().toString());
        }

        mCloudSettings = mSettingsDao.loadCloudSettings();
        if(mCloudSettings.getValue() == null) {
            Timber.d("Creating default cloud settings");
            mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new CloudSettings()));
        } else {
            Timber.d(mCloudSettings.getValue().toString());
        }
    }

    public LiveData<LocalSettings> getLocalSettings() {
        return mLocalSettings;
    }

    public void updateLocationSettings(double lat, double lon) {
        LocalSettings settings = mLocalSettings.getValue();
        settings.setLatitude(lat);
        settings.setLongitude(lon);
        mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(settings));
    }

    public LiveData<CloudSettings> getCloudSettings() {
        return mCloudSettings;
    }
}
