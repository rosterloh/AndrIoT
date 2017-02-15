package com.rosterloh.andriot.sensors;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

public class NeoPixel implements AutoCloseable {

    private static String TAG = NeoPixel.class.getSimpleName();

    public static final int MAX_BRIGHTNESS = 255;

    // RGB LED strip settings that have sensible defaults.
    private int brightness = MAX_BRIGHTNESS >> 1; // default to half

    // For peripherals access
    private SpiDevice device = null;

    public NeoPixel(String port) throws IOException {

        PeripheralManagerService pioService = new PeripheralManagerService();
        device = pioService.openSpiDevice(port);
        try {
            configure(device);
        } catch (IOException|RuntimeException e) {
            try {
                close();
            } catch (IOException|RuntimeException ignored) {
            }
            throw e;
        }
    }

    private void configure(SpiDevice device) throws IOException {

        device.setMode(SpiDevice.MODE0);    // Low clock, leading edge transfer
        device.setFrequency(6000000);       // 6 MHz
        device.setBitsPerWord(8);           // 8 BPW
        device.setBitJustification(false);  // MSB first
    }

    /**
     * Sets the brightness for all LEDs in the strip.
     * @param ledBrightness The brightness of the LED strip, between 0 and {@link #MAX_BRIGHTNESS}.
     */
    public void setBrightness(int ledBrightness) {
        if (ledBrightness < 0 || ledBrightness > MAX_BRIGHTNESS) {
            throw new IllegalArgumentException("Brightness needs to be between 0 and "
                    + MAX_BRIGHTNESS);
        }
        brightness = ledBrightness;
    }

    /**
     * Get the current brightness level
     */
    public int getBrightness() {
        return brightness;
    }

    /**
     * Writes the current RGB Led data to the peripheral bus.
     * @throws IOException
     */
    public void setColour(int r, int g, int b) throws IOException {

        if (device == null) {
            throw new IllegalStateException("SPI device not open");
        }

        byte[] values = new byte[] {
                (byte) ((r * brightness) >> 8),
                (byte) ((g * brightness) >> 8),
                (byte) ((b * brightness) >> 8),
        };

        device.write(values, values.length);
    }

    /**
     * Releases the SPI interface and related resources.
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
}
