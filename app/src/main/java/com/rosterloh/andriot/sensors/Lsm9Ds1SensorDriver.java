package com.rosterloh.andriot.sensors;

import android.hardware.Sensor;

import com.google.android.things.userdriver.UserDriverManager;
import com.google.android.things.userdriver.UserSensor;
import com.google.android.things.userdriver.UserSensorDriver;
import com.google.android.things.userdriver.UserSensorReading;

import java.io.IOException;
import java.util.UUID;

public class Lsm9Ds1SensorDriver implements AutoCloseable {

    private static final String TAG = Lsm9Ds1SensorDriver.class.getSimpleName();

    // DRIVER parameters
    // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
    // datasheet at http://www.st.com/content/ccc/resource/technical/document/datasheet/1e/3f/2a/d6/25/eb/48/46/DM00103319.pdf/files/DM00103319.pdf/jcr:content/translations/en.DM00103319.pdf
    private static final String DRIVER_VENDOR = "ST";
    private static final String DRIVER_NAME = "LSM9DS1";

    private Lsm9Ds1 device;

    private TemperatureUserDriver temperatureUserDriver;

    /**
     * Create a new framework sensor driver connected on the given bus.
     * The driver emits {@link android.hardware.Sensor} with pressure and temperature data when
     * registered.
     *
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     * @see #registerTemperatureSensor()
     */
    public Lsm9Ds1SensorDriver(String bus) throws IOException {
        device = new Lsm9Ds1(bus);
    }

    /**
     * Close the driver and the underlying device.
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        unregisterTemperatureSensor();
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }

    /**
     * Register a {@link UserSensor} that pipes temperature readings into the Android SensorManager.
     *
     * @see #unregisterTemperatureSensor()
     */
    public void registerTemperatureSensor() {
        if (device == null) {
            throw new IllegalStateException("cannot register closed driver");
        }

        if (temperatureUserDriver == null) {
            temperatureUserDriver = new TemperatureUserDriver();
            UserDriverManager.getManager().registerSensor(temperatureUserDriver.getUserSensor());
        }
    }

    /**
     * Unregister the temperature {@link UserSensor}.
     */
    public void unregisterTemperatureSensor() {
        if (temperatureUserDriver != null) {
            UserDriverManager.getManager().unregisterSensor(temperatureUserDriver.getUserSensor());
            temperatureUserDriver = null;
        }
    }

    private class TemperatureUserDriver extends UserSensorDriver {
        // DRIVER parameters
        // documented at https://source.android.com/devices/sensors/hal-interface.html#sensor_t
        private static final int TEMP_MAX_FREQ_HZ = 59;
        private static final int TEMP_MIN_FREQ_HZ = 50;
        private static final float MIN_TEMP_C = -40f;
        private static final float MAX_TEMP_C = 85f;
        private static final float MAX_POWER_CONSUMPTION_UA = 600f;
        private final int DRIVER_MIN_DELAY_US = Math.round(1000000.f/TEMP_MAX_FREQ_HZ);
        private final int DRIVER_MAX_DELAY_US = Math.round(1000000.f/TEMP_MIN_FREQ_HZ);
        private static final float DRIVER_MAX_RANGE = MAX_TEMP_C;
        private static final float DRIVER_RESOLUTION = 0.005f;
        private static final float DRIVER_POWER = MAX_POWER_CONSUMPTION_UA / 1000.f;
        private static final int DRIVER_VERSION = 1;
        private static final String DRIVER_REQUIRED_PERMISSION = "";

        private boolean enabled;
        private UserSensor userSensor;

        private UserSensor getUserSensor() {
            if (userSensor == null) {
                userSensor = UserSensor.builder()
                        .setType(Sensor.TYPE_AMBIENT_TEMPERATURE)
                        .setName(DRIVER_NAME)
                        .setVendor(DRIVER_VENDOR)
                        .setVersion(DRIVER_VERSION)
                        .setMaxRange(DRIVER_MAX_RANGE)
                        .setResolution(DRIVER_RESOLUTION)
                        .setPower(DRIVER_POWER)
                        .setMinDelay(DRIVER_MIN_DELAY_US)
                        .setRequiredPermission(DRIVER_REQUIRED_PERMISSION)
                        .setMaxDelay(DRIVER_MAX_DELAY_US)
                        .setUuid(UUID.randomUUID())
                        .setDriver(this)
                        .build();
            }
            return userSensor;
        }

        @Override
        public UserSensorReading read() throws IOException {
            return new UserSensorReading(new float[]{device.readTemperature()});
        }

        @Override
        public void setEnabled(boolean enabled) throws IOException {
            this.enabled = enabled;
        }

        private boolean isEnabled() {
            return enabled;
        }
    }
}
