package com.rosterloh.andriot;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.widget.Toast;

import com.rosterloh.andriot.databinding.ActivityMainBinding;
import com.rosterloh.andriot.databinding.Info;
import com.rosterloh.andriot.utils.AppPreference;
import com.rosterloh.andriot.utils.WeatherUtils;
import com.rosterloh.andriot.weather.Weather;
import com.rosterloh.andriot.weather.WeatherApi;
import com.rosterloh.andriot.weather.WeatherResponse;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Flowable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private ActivityMainBinding binding;

    private ConnectionDetector connectionDetector;
    private SensorManager sensorManager;
    //private Lsm9Ds1SensorDriver sensorDriver;
    //private float lastTemperature;

    private DeskCamera camera;
    private Handler cameraHandler;
    private HandlerThread cameraThread;

    private CompositeDisposable compositeDisposable;

    private void updateCurrentWeather(Weather weather) {

        AppPreference.saveLastUpdateTimeMillis(MainActivity.this);

        binding.tvWeatherIcon.setText(WeatherUtils.getStrIcon(MainActivity.this, weather.getIconId()));
        binding.tvTemperature.setText(weather.getTemp());
        binding.tvDescription.setText(weather.getDescription());
        binding.tvLastUpdate.setText(getString(R.string.last_update_label, weather.getLastUpdated()));
    }

    /*
        // Callback used when we register the LSM9DS1 sensor driver with the system's SensorManager.
        private SensorManager.DynamicSensorCallback dynamicSensorCallback
                = new SensorManager.DynamicSensorCallback() {
            @Override
            public void onDynamicSensorConnected(Sensor sensor) {
                if (sensor.getType() == Sensor.TYPE_AMBIENT_TEMPERATURE) {
                    // Our sensor is connected. Start receiving temperature data.
                    sensorManager.registerListener(temperatureListener, sensor,
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
            }

            @Override
            public void onDynamicSensorDisconnected(Sensor sensor) {
                super.onDynamicSensorDisconnected(sensor);
            }
        };

        // Callback when SensorManager delivers temperature data.
        private SensorEventListener temperatureListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                lastTemperature = event.values[0];
                Log.d(TAG, "sensor changed: " + lastTemperature);
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                Log.d(TAG, "accuracy changed: " + accuracy);
            }
        };
    */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        connectionDetector = new ConnectionDetector(MainActivity.this);
        Info info = new Info(connectionDetector);
        binding.setInfo(info);

        compositeDisposable = new CompositeDisposable();

        setupTextViews();
        setupWeather();
        setupSensors();
        setupCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        compositeDisposable.clear();
        /*
        // Clean up sensor registrations
        sensorManager.unregisterListener(temperatureListener);
        sensorManager.unregisterDynamicSensorCallback(dynamicSensorCallback);

        // Clean up peripheral.
        if (sensorDriver != null) {
            try {
                sensorDriver.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            sensorDriver = null;
        }*/

        camera.shutDown();
        cameraThread.quitSafely();
    }

    void setupTextViews() {

        Typeface weatherFontIcon = Typeface.createFromAsset(this.getAssets(),
                "fonts/weathericons-regular-webfont.ttf");
        Typeface robotoThin = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Thin.ttf");
        Typeface robotoBlack = Typeface.createFromAsset(this.getAssets(),
                "fonts/Roboto-Black.ttf");

        binding.tvWeatherIcon.setTypeface(weatherFontIcon);
        binding.tvTemperature.setTypeface(robotoThin);
        binding.tvDescription.setTypeface(robotoThin);
        binding.tvLastUpdate.setTypeface(robotoThin);
        binding.tcTime.setTypeface(robotoBlack);
        binding.tcDate.setTypeface(robotoBlack);
        binding.tvWifiInfo.setTypeface(robotoThin);
        binding.tvEthInfo.setTypeface(robotoThin);
    }

    void setupWeather() {

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(interceptor).build();

        final WeatherApi weatherApi = new Retrofit.Builder()
                .baseUrl("http://api.openweathermap.org/data/2.5/")
                .client(client)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create()) // RxJava2CallAdapterFactory.createWithScheduler(Schedulers.io())
                .addConverterFactory(GsonConverterFactory.create())
                .build().create(WeatherApi.class);

        compositeDisposable.add(Flowable.interval(0, 20, TimeUnit.MINUTES)
                .flatMap(new Function<Long, Flowable<WeatherResponse>>() {
                    @Override
                    public Flowable<WeatherResponse> apply(Long aLong) throws Exception {
                        Log.d(TAG, "Getting new data");
                        return weatherApi.getCurrentWeatherConditions("51.621203", "-1.294148", "2e9e498e77b879ea237e3a571c57f1fa", "metric");
                    }
                })
                .flatMap(new Function<WeatherResponse, Flowable<Weather>>() {
                    @Override
                    public Flowable<Weather> apply(WeatherResponse response) throws Exception {
                        Log.d(TAG, "Parsing received data");
                        return Flowable.just(new Weather.Builder()
                                .temperature(response.getMain().getTemp().intValue() + "º")
                                .description(response.getWeather().get(0).getDescription())
                                .iconId(response.getWeather().get(0).getIcon())
                                .lastUpdated(new SimpleDateFormat("H:mm", Locale.getDefault()).format(new Date((long) response.getDt() * 1000)))
                                .build());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Consumer<Weather>() {
                    @Override
                    public void accept(Weather weather) throws Exception {
                        Log.d(TAG, "Updating display");
                        updateCurrentWeather(weather);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e(TAG, "Error: " + throwable.getMessage());
                        Toast.makeText(MainActivity.this,
                                getString(R.string.toast_parse_error),
                                Toast.LENGTH_SHORT).show();
                    }
                })
        );
    }

    void setupSensors() {

        sensorManager = ((SensorManager) getSystemService(SENSOR_SERVICE));

        List<Sensor> sensors = sensorManager.getDynamicSensorList(Sensor.TYPE_ALL);
        for ( Sensor s : sensors) {
            Log.d(TAG, "Sensor " + s.getName() + " (" + s.getId() + ") " + " is " + s.getType());
        }

        /*
        try {
            sensorDriver = new Lsm9Ds1SensorDriver("I2C1");
            sensorManager.registerDynamicSensorCallback(dynamicSensorCallback);
            sensorDriver.registerTemperatureSensor();
            Log.d(TAG, "Initialised I2C LSM9DS1");
        } catch (IOException e) {
            throw new RuntimeException("Error initialising LSM9DS1"+ e);
        }*/
    }

    void setupCamera() {

        // We need permission to access the camera
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // A problem occurred auto-granting the permission
            Log.d(TAG, "No camera permission");
        } else {
            cameraThread = new HandlerThread("CameraBackground");
            cameraThread.start();
            cameraHandler = new Handler(cameraThread.getLooper());

            camera = DeskCamera.getInstance();
            camera.initialiseCamera(this, cameraHandler, onImageAvailableListener);
        }
    }

    private ImageReader.OnImageAvailableListener onImageAvailableListener =
            new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader reader) {
            Image image = reader.acquireLatestImage();

            // get image bytes
            ByteBuffer imageBuf = image.getPlanes()[0].getBuffer();
            final byte[] imageBytes = new byte[imageBuf.remaining()];
            imageBuf.get(imageBytes);
            image.close();

            binding.ivCamera.setImageBitmap(BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length));
        }
    };
}
