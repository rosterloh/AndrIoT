package com.rosterloh.andriot.sensors;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

public class NeoPixel implements AutoCloseable {

    private static String TAG = NeoPixel.class.getSimpleName();

    /**
     * Color ordering for the RGB LED messages; the most common modes are BGR and RGB.
     */
    public enum Mode {
        RGB,
        RBG,
        GRB,
        GBR,
        BRG,
        BGR
    }

    /**
     * The direction to apply colors when writing LED data
     */
    public enum Direction {
        NORMAL,
        REVERSED,
    }

    private static final int SPI_BPW = 8; // Bits per word
    private static final int SPI_FREQUENCY = 1000000;
    private static final int SPI_MODE = 2;

    // For peripherals access
    private SpiDevice device = null;

    public NeoPixel(String port, Mode mode, Direction direction) throws IOException {

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
        // Note: You may need to set bit justification for your board.
        // this.device.setBitJustification(SPI_BITJUST);
        device.setFrequency(SPI_FREQUENCY);
        device.setMode(SPI_MODE);
        device.setBitsPerWord(SPI_BPW);
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
