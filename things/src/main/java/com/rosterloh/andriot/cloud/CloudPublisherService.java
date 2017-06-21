package com.rosterloh.andriot.cloud;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.WorkerThread;
import android.util.Log;

import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class CloudPublisherService extends Service {

    private static final String TAG = CloudPublisherService.class.getSimpleName();

    private static final int BUFFER_SIZE_FOR_ONCHANGE_SENSORS = 10;
    private static final long PUBLISH_INTERVAL_MS = TimeUnit.SECONDS.toMillis(20);
    private static final long ERRORS_TO_INITIATE_BACKOFF = 20;
    private static final long BACKOFF_INTERVAL_MS = TimeUnit.MINUTES.toMillis(1);

    private ScheduledExecutorService executor;
    private MQTTPublisher publisher;

    private AtomicInteger unsuccessfulTentatives = new AtomicInteger(0);
    private final ConcurrentHashMap<String, SensorData> mostRecentData = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, PriorityBlockingQueue<SensorData>> onChangeData =
            new ConcurrentHashMap<>();

    private final Runnable sensorConsumerRunnable = () -> {
        long delayForNextTentative = PUBLISH_INTERVAL_MS;
        try {
            processCollectedSensorData();
            unsuccessfulTentatives.set(0);
        } catch (Throwable t) {
            if (unsuccessfulTentatives.get() >= ERRORS_TO_INITIATE_BACKOFF) {
                delayForNextTentative = BACKOFF_INTERVAL_MS;
            } else {
                unsuccessfulTentatives.incrementAndGet();
            }
            Log.e(TAG, String.format(Locale.getDefault(),
                    "Cannot publish. %d unsuccessful tentatives, will try again in %d ms",
                    unsuccessfulTentatives.get(), delayForNextTentative), t);
        }
    };

    @WorkerThread
    private void processCollectedSensorData() {
        if (publisher == null || !publisher.isReady()) {
            return;
        }
        ArrayList<SensorData> data = new ArrayList<>();

        // get sensorData from continuous sensors
        for (String sensorName : mostRecentData.keySet()) {
            data.add(mostRecentData.remove(sensorName));
        }

        // get sensorData from onChange sensors
        for (String sensorName : onChangeData.keySet()) {
            onChangeData.get(sensorName).drainTo(data);
        }

        Log.i(TAG, "publishing " + data.size() + " sensordata elements");
        publisher.publish(data);
    }

    private final IBinder binder = new CloudPublisherService.LocalBinder();

    public class LocalBinder extends Binder {
        public CloudPublisherService getService() {
            return CloudPublisherService.this;
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleWithFixedDelay(sensorConsumerRunnable, 0, PUBLISH_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        executor.shutdown();
    }
}
