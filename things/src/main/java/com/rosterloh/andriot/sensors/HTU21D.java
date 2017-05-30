package com.rosterloh.andriot.sensors;

import android.support.annotation.IntDef;

import com.google.android.things.pio.I2cDevice;
import com.google.android.things.pio.PeripheralManagerService;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;

import timber.log.Timber;

public class HTU21D implements AutoCloseable {

    private static final boolean DEBUG = false;

    public static final String DEFAULT_BUS = "I2C1";
    public static final int I2C_ADDRESS_40 = 0x40;
    public static final float MIN_TEMP_C = -40f;
    public static final float MAX_TEMP_C = 125f;

    @Retention(RetentionPolicy.SOURCE)
    @IntDef({MODE_12_14, MODE_8_12, MODE_10_13, MODE_11_11})
    public @interface Mode {}
    public static final int MODE_12_14 = 0;
    public static final int MODE_8_12  = 1;
    public static final int MODE_10_13 = 2;
    public static final int MODE_11_11 = 3;

    private static final int HTU21D_REG_TEMP = 0xE3;
    private static final int HTU21D_REG_HUM = 0xE5;
    private static final int HTU21D_REG_USER = 0xE7;
    private static final int HTU21D_REG_RESET = 0xFE;

    private static final int HTU21D_RESOLUTION_MASK = 0b10000001;

    private I2cDevice i2cDevice;
    private final byte[] buffer = new byte[2]; // for reading sensor values
    private int[] values = new int[10];
    private int resolution;
    private int temperature_resolution;
    private int humidity_resolution;

    public HTU21D(String bus) throws IOException {
        PeripheralManagerService pioService = new PeripheralManagerService();
        I2cDevice device = pioService.openI2cDevice(bus, I2C_ADDRESS_40);
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

    public HTU21D() throws IOException {
        this(DEFAULT_BUS);
    }

    private void connect(I2cDevice device) throws IOException {
        i2cDevice = device;

        int user = device.readRegByte(HTU21D_REG_USER) & 0xff;
        resolution = user & HTU21D_RESOLUTION_MASK;
        switch (resolution) {
            case MODE_12_14:
                Timber.d("Measurement resolution RH 12 bits Temp 14 bits");
                temperature_resolution = 0x3f;
                humidity_resolution = 0x0f;
                break;
            case MODE_8_12:
                Timber.d("Measurement resolution RH 8 bits Temp 12 bits");
                temperature_resolution = 0x00;
                humidity_resolution = 0x0f;
                break;
            case MODE_10_13:
                Timber.d("Measurement resolution RH 10 bits Temp 13 bits");
                temperature_resolution = 0x1f;
                humidity_resolution = 0x03;
                break;
            case MODE_11_11:
                Timber.d("Measurement resolution RH 11 bits Temp 11 bits");
                temperature_resolution = 0x07;
                humidity_resolution = 0x07;
                break;
        }

        device.writeRegByte(HTU21D_REG_RESET, (byte) 1);
        // Sleep 20ms
    }

    @Override
    public void close() throws IOException {
        if (i2cDevice != null) {
            try {
                i2cDevice.close();
            } finally {
                i2cDevice = null;
            }
        }
    }

    public @Mode int getResolution() {
        return resolution;
    }

    public float readTemperature() throws IOException, IllegalStateException {
        int rawTemp = readSample(HTU21D_REG_TEMP, true);
        return compensateTemperature(rawTemp);
    }

    public float readHumidity() throws IOException {
        int rawHum = readSample(HTU21D_REG_HUM, false);
        return compensateHumidity(rawHum);
    }

    public float[] readTemperatureAndHumidity() throws IOException {
        int rawTemp = readSample(HTU21D_REG_TEMP, true);
        float temperature = compensateTemperature(rawTemp);
        int rawHumidity = readSample(HTU21D_REG_HUM, false);
        float humidity = compensateHumidity(rawHumidity);
        return new float[]{temperature, humidity};
    }

    private int readSample(int address, boolean temp) throws IOException, IllegalStateException {
        if (i2cDevice == null) {
            throw new IllegalStateException("I2C device not open");
        }

        double average = 0;
        int div = values.length;
        for (int i = 0; i < values.length; i++) {
            i2cDevice.readRegBuffer(address, buffer, 2);
            values[i] = (((buffer[0] & 0xFF) << 8) | (buffer[1] & 0xFF ));
            if (values[i] > 0 && values[i] != 47103) { // quirk?
                average += values[i];
            } else {
                div--;
            }
        }

        Timber.d(Arrays.toString(values));

        if (div > 0) {
            return (int) average / div;
        } else {
            return values[0];
        }
    }

    /**
     * Formula T = -46.85 + 175.72 * ST / 2^16 from datasheet p14
     * @param rawTemp raw temperature value read from device
     * @return temperature in °C range from -40°C to +125°C
     */
    static float compensateTemperature(int rawTemp) {
        int temp = ((21965 * (rawTemp & 0xFFFC)) >> 13) - 46850;
        if (DEBUG) Timber.d("Raw: " + rawTemp + " Temp: " + temp);
        return (float) temp / 1000;
    }

    /**
     * Formula RH = -6 + 125 * SRH / 2^16 from datasheet p14
     * @param rawHumidity raw humidity value read from device
     * @return relative humidity RH% range from 0-100
     */
    static float compensateHumidity(int rawHumidity) {
        int hum = ((15625 * (rawHumidity & 0xFFFC)) >> 13) - 6000;
        if (DEBUG) Timber.d("Raw: " + rawHumidity + " Hum: " + hum);
        return (float) hum / 1000;
    }
}
