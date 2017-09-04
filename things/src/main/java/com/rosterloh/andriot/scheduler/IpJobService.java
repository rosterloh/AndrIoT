package com.rosterloh.andriot.scheduler;

import android.app.job.JobParameters;
import android.app.job.JobService;

import com.rosterloh.andriot.db.SettingsRepository;
import com.rosterloh.andriot.util.NetworkUtils;

import javax.inject.Inject;

import timber.log.Timber;

public class IpJobService extends JobService {

    @Inject
    SettingsRepository mSettingsRepository;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {

        mSettingsRepository.setIpAddress(NetworkUtils.getIPAddress(true));

        // Return true as there's more work to be done with this job.
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        Timber.i("on stop job: " + params.getJobId());

        // Return false to drop the job.
        return false;
    }
}
