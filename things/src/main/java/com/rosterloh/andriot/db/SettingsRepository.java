package com.rosterloh.andriot.db;

import android.app.job.JobInfo;
import android.arch.lifecycle.LiveData;
import android.content.ComponentName;

import com.rosterloh.andriot.AppExecutors;
import com.rosterloh.andriot.scheduler.IpJobService;
import com.rosterloh.andriot.scheduler.ThingsScheduler;

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
    private final ThingsScheduler mScheduler;

    private LiveData<LocalSettings> mLocalSettings;
    private LiveData<CloudSettings> mCloudSettings;

    @Inject
    SettingsRepository(AppExecutors appExecutors, SettingsDao settingsDao, ThingsScheduler scheduler) {
        mAppExecutors = appExecutors;
        mSettingsDao = settingsDao;
        mScheduler = scheduler;

        mLocalSettings = mSettingsDao.loadLocalSettings();
        mLocalSettings.observeForever(settings -> {
            if (settings == null) {
                Timber.d("Creating default local settings");
                mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new LocalSettings()));
            } else {
                Timber.d(mLocalSettings.getValue().toString());
                mScheduler.scheduleIpAddressService();
            }
        });

        mCloudSettings = mSettingsDao.loadCloudSettings();
        mCloudSettings.observeForever(settings -> {
            if (settings == null) {
                Timber.d("Creating default cloud settings");
                mAppExecutors.diskIO().execute(() -> mSettingsDao.insert(new CloudSettings()));
            } else {
                Timber.d(mCloudSettings.getValue().toString());
            }
        });
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

    public void setIpAddress(String ip) {
        LocalSettings settings = mLocalSettings.getValue();
        if (settings != null) {
            if (!settings.getIpAddress().equals(ip)) {
                Timber.d("IP changed to " + ip);
                settings.setIpAddress(ip);
                mAppExecutors.diskIO().execute(() -> mSettingsDao.update(settings));
            }
        }
    }
}
