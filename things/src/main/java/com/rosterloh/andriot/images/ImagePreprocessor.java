package com.rosterloh.andriot.images;

import android.graphics.Bitmap;
import android.media.Image;

import junit.framework.Assert;

/**
 * Class that process an Image and extracts a Bitmap in a format appropriate for
 * TensorFlowImageClassifier
 */
public class ImagePreprocessor {

    private static final boolean SAVE_PREVIEW_BITMAP = false;

    private Bitmap rgbFrameBitmap;
    private Bitmap croppedBitmap;

    private byte[][] cachedYuvBytes;
    private int[] cachedRgbBytes;

    public ImagePreprocessor(int inputImageWidth, int inputImageHeight, int outputSize) {
        this.cachedRgbBytes = new int[inputImageWidth * inputImageHeight];
        this.cachedYuvBytes = new byte[3][];
        this.croppedBitmap = Bitmap.createBitmap(outputSize, outputSize, Bitmap.Config.ARGB_8888);
        this.rgbFrameBitmap = Bitmap.createBitmap(inputImageWidth, inputImageHeight, Bitmap.Config.ARGB_8888);
    }

    public Bitmap preprocessImage(final Image image) {
        if (image == null) {
            return null;
        }

        Assert.assertEquals("Invalid size width", rgbFrameBitmap.getWidth(), image.getWidth());
        Assert.assertEquals("Invalid size height", rgbFrameBitmap.getHeight(), image.getHeight());

        cachedRgbBytes = ImageUtils.convertImageToBitmap(image, cachedRgbBytes, cachedYuvBytes);

        if (croppedBitmap != null && rgbFrameBitmap != null) {
            rgbFrameBitmap.setPixels(cachedRgbBytes, 0, image.getWidth(), 0, 0,
                    image.getWidth(), image.getHeight());
            ImageUtils.cropAndRescaleBitmap(rgbFrameBitmap, croppedBitmap, 0);
        }

        image.close();

        // For debugging
        if (SAVE_PREVIEW_BITMAP) {
            ImageUtils.saveBitmap(croppedBitmap);
        }
        return croppedBitmap;
    }
}
