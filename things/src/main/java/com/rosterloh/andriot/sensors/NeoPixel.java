package com.rosterloh.andriot.sensors;

import android.graphics.Color;

import com.google.android.things.pio.PeripheralManagerService;
import com.google.android.things.pio.SpiDevice;

import java.io.IOException;

public class NeoPixel implements AutoCloseable {

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

    public enum Direction {
        NORMAL,
        REVERSED,
    }

    // RGB LED strip configuration that must be provided by the caller.
    private Mode mMode;

    // Direction of the led strip;
    private Direction mDirection;

    // Device SPI Configuration constants
    private static final int PACKET_LENGTH = 3;
    private static final int SPI_BPW = 8; // Bits per word
    private static final int SPI_FREQUENCY = 1000000; // 1 MHz
    private static final int SPI_MODE = SpiDevice.MODE0; // Low clock, leading edge transfer

    // For peripherals access
    private SpiDevice mDevice = null;

    /**
     * Create a new NeoPixel driver.
     *
     * @param port Name of the SPI bus
     * @param mode The {@link Mode} indicating the red/green/blue byte ordering for the device.
     */
    public NeoPixel(String port, Mode mode) throws IOException {
        this(port, mode, Direction.NORMAL);
    }

    /**
     * Create a new NeoPixel driver.
     *
     * @param port      Name of the SPI bus
     * @param mode      The {@link Mode} indicating the red/green/blue byte ordering for the device.
     * @param direction The {@link Direction} or the led strip.
     */
    public NeoPixel(String port, Mode mode, Direction direction) throws IOException {
        mMode = mode;
        mDirection = direction;
        PeripheralManagerService pioService = new PeripheralManagerService();
        mDevice = pioService.openSpiDevice(port);
        try {
            configure(mDevice);
        } catch (IOException e) {
            try {
                close();
            } catch (IOException ignored) {
            }
            throw e;
        }
    }

    /**
     * Create a new Ws2801 driver.
     *
     * @param device The {@link SpiDevice} where the LED strip is attached to.
     * @param mode   The {@link Mode} indicating the red/green/blue byte ordering for the device.
     */
    /*package*/ NeoPixel(SpiDevice device, Mode mode, Direction direction) throws IOException {
        mMode = mode;
        mDirection = direction;
        mDevice = device;
        configure(mDevice);
    }

    private void configure(SpiDevice device) throws IOException {
        // Note: You may need to set bit justification for your board.
        //device.setBitJustification(false);  // MSB first
        device.setFrequency(SPI_FREQUENCY);
        device.setMode(SPI_MODE);
        device.setBitsPerWord(SPI_BPW);
    }

    /**
     * Returns an WS2801 packet corresponding to the current brightness and given {@link Color}.
     *
     * @param color The {@link Color} to retrieve the protocol packet for.
     * @return WS2801 packet corresponding to the current brightness and given {@link Color}.
     */
    private byte[] getColourData(int color) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);

        switch (mMode) {
            case RBG:
                return new byte[]{(byte) r, (byte) b, (byte) g};
            case BGR:
                return new byte[]{(byte) b, (byte) g, (byte) r};
            case BRG:
                return new byte[]{(byte) b, (byte) r, (byte) g};
            case GRB:
                return new byte[]{(byte) g, (byte) r, (byte) b};
            case GBR:
                return new byte[]{(byte) g, (byte) b, (byte) r};
            default:
                // RGB
                return new byte[]{(byte) r, (byte) g, (byte) b};
        }
    }

    /**
     * Writes the current RGB Led data to the peripheral bus.
     *
     * @param colours An array of integers corresponding to a {@link Color}.
     * @throws IOException if write fails
     * @throws IllegalStateException if SPI bus is not open
     */
    public void setColour(int[] colours) throws IOException, IllegalStateException {

        if (mDevice == null) {
            throw new IllegalStateException("SPI device not open");
        }

        byte[] ledData = new byte[PACKET_LENGTH * colours.length];

        // Compute the packets to send.
        for (int i = 0; i < colours.length; i++) {
            int outputPosition = i * PACKET_LENGTH;
            int di = mDirection == Direction.NORMAL ? i : colours.length - i - 1;
            System.arraycopy(getColourData(colours[di]), 0, ledData, outputPosition, PACKET_LENGTH);
        }

        mDevice.write(ledData, ledData.length);
    }

    /**
     * Releases the SPI interface and related resources.
     */
    @Override
    public void close() throws IOException {
        if (mDevice != null) {
            try {
                mDevice.close();
            } finally {
                mDevice = null;
            }
        }
    }
}
