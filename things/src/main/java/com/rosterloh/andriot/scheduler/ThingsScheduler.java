package com.rosterloh.andriot.scheduler;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class ThingsScheduler {

    private static final long IP_JOB_PERIOD_MILLIS = TimeUnit.MINUTES.toMillis(30);
    private static final long IP_JOB_FLEX_MILLIS = TimeUnit.MINUTES.toMillis(10);

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({IP_JOB_ID})
    public @interface ThingsJob {}
    private static final int IP_JOB_ID = 0;

    private final JobScheduler jobScheduler;
    private final ComponentName ipComponent;

    @Inject
    public ThingsScheduler(Context context) {
        jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
        ipComponent = new ComponentName(context, IpJobService.class);
    }

    public void cancelAllJobs() {
        jobScheduler.cancelAll();
    }

    public void scheduleIpAddressService() {
        JobInfo.Builder ipJob = new JobInfo.Builder(IP_JOB_ID, ipComponent)
                .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(IP_JOB_PERIOD_MILLIS, IP_JOB_FLEX_MILLIS);

        jobScheduler.schedule(ipJob.build());
    }
}
