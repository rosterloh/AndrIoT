package com.rosterloh.andriot.sensors;

import android.support.annotation.IntDef;
import android.util.Log;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;

public class Lsm9Ds1 implements AutoCloseable {

    private static final String TAG = Lsm9Ds1.class.getSimpleName();

    /**
     * Chip ID for the LSM9DS1 accelerometer/gyro
     */
    public static final int CHIP_ID_AG = 0x68;

    /**
     * Chip ID for the LSM9DS1 magnetometer
     */
    public static final int CHIP_ID_X = 0x3D;

    /**
     * I2C address for the accelerometer/gyro sensor.
     */
    public static final int I2C_ADDRESS_AG = 0x1E;

    /**
     * I2C address for the magnetometer sensor.
     */
    public static final int I2C_ADDRESS_X = 0x6B;

    private static final int LSM9DS1_REG_ID = 0x0F;
    private static final int LSM9DS1_REG_TEMP_L = 0x15;
    private static final int LSM9DS1_REG_TEMP_H = 0x16;

    private I2cDevice device;
    private final byte[] buffer = new byte[2]; // for reading sensor values
    private int chipId;
    private int mode;

    /**
     * Power mode.
     */
    @IntDef({MODE_STANDBY, MODE_ACTIVE})
    public @interface Mode {}

    public static final int MODE_STANDBY = 0; // i2c on, output off, low power
    public static final int MODE_ACTIVE = 1; // i2c on, output on

    /**
     * Create a new LSM9DS1 sensor driver connected on the given bus.
     * @param bus I2C bus the sensor is connected to.
     * @throws IOException
     */
    public Lsm9Ds1(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS_AG);
        try {
            connect(device);
        } catch (IOException|RuntimeException e) {
            try {
                close();
            } catch (IOException|RuntimeException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new LSM9DS1 driver connected to the given I2C device.
     * @param device
     * @throws IOException
     */
    /*package*/ Lsm9Ds1(I2cDevice device) throws IOException {
        connect(device);
    }

    private void connect(I2cDevice device) throws IOException {
        this.device = device;

        chipId = device.readRegByte(LSM9DS1_REG_ID);
        if (chipId != CHIP_ID_AG) {
            Log.w(TAG, "Unexpected chip id " + chipId);
        }
    }

    /**
     * Close the driver and the underlying device.
     */
    @Override
    public void close() throws IOException {
        if (device != null) {
            try {
                device.close();
            } finally {
                device = null;
            }
        }
    }

    /**
     * Set current power mode.
     * @param mode
     * @throws IOException
     * @throws IllegalStateException
     */
    public void setMode(@Mode int mode) throws IOException, IllegalStateException {
        this.mode = mode;
    }

    /**
     * Returns the sensor chip ID.
     */
    public int getChipId() {
        return chipId;
    }

    /**
     * Read the current temperature.
     *
     * @return the current temperature in degrees Celsius
     */
    public int readTemperature() throws IOException, IllegalStateException {
        if (device == null) {
            throw new IllegalStateException("I2C device is already closed");
        }
        synchronized (buffer) {
            device.readRegBuffer(LSM9DS1_REG_TEMP_L, buffer, 2);
            return (buffer[1] >> 8 | buffer[0]);
        }
    }
}
