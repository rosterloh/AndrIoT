package com.rosterloh.andriot.sensors;

import android.content.Context;
import android.graphics.ImageFormat;
import android.graphics.PixelFormat;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.ImageReader;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.util.Size;

import java.util.Collections;

import timber.log.Timber;

import static android.content.Context.CAMERA_SERVICE;

public class DeskCamera {

    public static final int IMAGE_WIDTH = 640;
    public static final int IMAGE_HEIGHT = 480;

    private static final int MAX_IMAGES = 1;
    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;

    /**
     * An {@link ImageReader} that handles still image capture.
     */
    private ImageReader mImageReader;

    /**
     * Callback handling device state changes
     */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice device) {
            Timber.d("Opened camera.");
            mCameraDevice = device;
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice device) {
            Timber.d("Camera disconnected, closing.");
            device.close();
        }

        @Override
        public void onError(@NonNull CameraDevice device, int i) {
            Timber.d("Camera device error, closing.");
            device.close();
        }

        @Override
        public void onClosed(@NonNull CameraDevice device) {
            Timber.d("Closed camera, releasing");
            mCameraDevice = null;
        }
    };

    /**
     * Callback handling session state changes
     */
    private CameraCaptureSession.StateCallback mSessionCallback = new CameraCaptureSession.StateCallback() {
        @Override
        public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
            // The camera is already closed
            if (mCameraDevice == null) {
                return;
            }

            // When the session is ready, we start capture.
            mCaptureSession = cameraCaptureSession;
            triggerImageCapture();
        }

        @Override
        public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
            Timber.w("Failed to configure camera");
        }
    };

    /**
     * Callback handling capture session events
     */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback = new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            Timber.d("Partial result");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {

            session.close();
            mCaptureSession = null;
            Timber.d("CaptureSession closed");
        }
    };

    // Lazy-loaded singleton, so only one instance of the camera is created.
    public DeskCamera() {
    }

    /**
     * Initialise the camera device
     */
    public void initialiseCamera(Context context,
                                 Handler backgroundHandler,
                                 ImageReader.OnImageAvailableListener imageAvailableListener) {

        //dumpFormatInfo(context);
        // Discover the camera instance
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting IDs", e);
        }
        if (camIds.length < 1) {
            Timber.d("No cameras found");
            return;
        }
        String id = camIds[0];
        Timber.d("Using camera id " + id);

        // Initialise the image processor
        mImageReader = ImageReader.newInstance(IMAGE_WIDTH, IMAGE_HEIGHT,
                ImageFormat.YUV_420_888, MAX_IMAGES);
        mImageReader.setOnImageAvailableListener(
                imageAvailableListener, backgroundHandler);

        // Open the camera resource
        try {
            manager.openCamera(id, mStateCallback, backgroundHandler);
        } catch (CameraAccessException cae) {
            Timber.d("Camera access exception", cae);
        }
    }

    /**
     * Begin a still image capture
     */
    public void takePicture() {
        if (mCameraDevice == null) {
            Timber.w("Cannot capture image. Camera not initialised.");
            return;
        }

        // Here, we create a CameraCaptureSession for capturing still images.
        try {
            mCameraDevice.createCaptureSession(
                    Collections.singletonList(mImageReader.getSurface()),
                    mSessionCallback,
                    null);
        } catch (CameraAccessException cae) {
            Timber.d("access exception while preparing pic", cae);
        }
    }

    /**
     * Execute a new capture request within the active session
     */
    private void triggerImageCapture() {
        try {
            final CaptureRequest.Builder captureBuilder
                    = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(mImageReader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_MODE, CaptureRequest.CONTROL_AWB_MODE_AUTO);
            Timber.d("Session initialised.");
            mCaptureSession.capture(captureBuilder.build(), mCaptureCallback, null);
        } catch (CameraAccessException cae) {
            Timber.d("camera capture exception");
        }
    }

    /**
     * Close the camera resources
     */
    public void shutDown() {
        if (mCameraDevice != null) {
            mCameraDevice.close();
        }
    }

    /**
     * Helpful debugging method:  Dump all supported camera formats to log.  You don't need to run
     * this for normal operation, but it's very helpful when porting this code to different
     * hardware.
     */
    public static void dumpFormatInfo(Context context) {
        CameraManager manager = (CameraManager) context.getSystemService(CAMERA_SERVICE);
        String[] camIds = {};
        try {
            camIds = manager.getCameraIdList();
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting IDs");
        }
        if (camIds.length < 1) {
            Timber.d("No cameras found");
        }
        String id = camIds[0];
        Timber.d("Using camera id " + id);
        try {
            CameraCharacteristics characteristics = manager.getCameraCharacteristics(id);
            StreamConfigurationMap configs = characteristics.get(
                    CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            for (int format : configs.getOutputFormats()) {
                StringBuilder sb = new StringBuilder();
                switch (format) {
                    case PixelFormat.RGBA_8888:
                        sb.append("RGBA_8888 ");
                        break;
                    case ImageFormat.PRIVATE:
                        sb.append("PRIVATE ");
                        break;
                    case ImageFormat.YUV_420_888:
                        sb.append("YUV_420_888 ");
                        break;
                    case ImageFormat.JPEG:
                        sb.append("JPEG ");
                        break;
                    default:
                        sb.append("0x");
                        sb.append(Integer.toHexString(format));
                        sb.append(" ");
                        break;
                }
                sb.append("supports ");
                for (Size s : configs.getOutputSizes(format)) {
                    sb.append(s.toString());
                }
                Timber.d(sb.toString());
            }
            int[] effects = characteristics.get(CameraCharacteristics.CONTROL_AVAILABLE_EFFECTS);
            StringBuilder sb = new StringBuilder();
            sb.append("Effects available: ");
            for (int effect : effects) {
                switch (effect) {
                    case CaptureRequest.CONTROL_EFFECT_MODE_OFF:
                        sb.append("OFF ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_MONO:
                        sb.append("MONO ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_NEGATIVE:
                        sb.append("NEGATIVE ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_SOLARIZE:
                        sb.append("SOLARIZE ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_SEPIA:
                        sb.append("SEPIA ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_POSTERIZE:
                        sb.append("POSTERIZE ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_WHITEBOARD:
                        sb.append("WHITEBOARD ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_BLACKBOARD:
                        sb.append("BLACKBOARD ");
                        break;
                    case CaptureRequest.CONTROL_EFFECT_MODE_AQUA:
                        sb.append("AQUA ");
                        break;
                    default:
                        sb.append("0x");
                        sb.append(Integer.toHexString(effect));
                        sb.append(" ");
                        break;
                }
            }
            Timber.d(sb.toString());
        } catch (CameraAccessException e) {
            Timber.d("Cam access exception getting characteristics.");
        }
    }
}
