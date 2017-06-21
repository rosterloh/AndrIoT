package com.rosterloh.andriot.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.ParcelUuid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import timber.log.Timber;

import static android.content.Context.BLUETOOTH_SERVICE;

public class GattServer {

    private static final String TAG = GattServer.class.getSimpleName();
    public static UUID DESCRIPTOR_CONFIG = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
    public static UUID DESCRIPTOR_USER_DESC = UUID.fromString("00002901-0000-1000-8000-00805f9b34fb");
    public static UUID SERVICE_UUID = UUID.fromString("15b63b79-ddbe-43f1-a53e-763690979de5");
    public static UUID CHARACTERISTIC_UUID = UUID.fromString("b26294c0-4f3b-44b3-b85a-a248975c8146");

    private static final ParcelUuid EDDYSTONE_SERVICE_UUID =
            ParcelUuid.fromString("0000FEAA-0000-1000-8000-00805F9B34FB");

    private static final String NAMESPACE_ID = "0003eebe2dd9d71889f4";
    private static final String INSTANCE_ID = "6a074a";

    public interface GattServerListener {

        void onWriteRequested();
        byte[] onReadRequested();

    }
    private Context appContext;
    private GattServerListener btListener;
    private BluetoothManager bluetoothManager;
    private BluetoothGattServer bluetoothGattServer;
    private BluetoothLeAdvertiser bluetoothLeAdvertiser;
    private Set<BluetoothDevice> registeredDevices = new HashSet<>();

    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);

            switch (state) {
                case BluetoothAdapter.STATE_ON:
                    startAdvertising();
                    startServer();
                    break;
                case BluetoothAdapter.STATE_OFF:
                    stopServer();
                    stopAdvertising();
                    break;
                default:
                    // Do nothing
                    break;
            }
        }
    };

    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {
        @Override
        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
            Timber.d("LE Advertise Started.");
        }

        @Override
        public void onStartFailure(int errorCode) {
            Timber.w("LE Advertise Failed: " + errorCode);
        }
    };

    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Timber.d("BluetoothDevice CONNECTED: " + device);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Timber.d("BluetoothDevice DISCONNECTED: " + device);
                // Remove device from any active subscriptions
                registeredDevices.remove(device);
            }
        }

        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                byte[] value = btListener.onReadRequested();
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, value);
            } else {
                // Invalid characteristic
                Timber.w("Invalid Characteristic Read: " + characteristic.getUuid());
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }

        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded,
                                                 int offset, byte[] value) {
            if (CHARACTERISTIC_UUID.equals(characteristic.getUuid())) {
                if (btListener != null) {
                    btListener.onWriteRequested();
                }
                notifyRegisteredDevices();
            } else {
                // Invalid characteristic
                Timber.w("Invalid Characteristic Write: " + characteristic.getUuid());
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            if (DESCRIPTOR_CONFIG.equals(descriptor.getUuid())) {
                Timber.d("Config descriptor read request");
                byte[] returnValue;
                if (registeredDevices.contains(device)) {
                    returnValue = BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE;
                } else {
                    returnValue = BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE;
                }
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, returnValue);
            } else if (DESCRIPTOR_USER_DESC.equals(descriptor.getUuid())) {
                Timber.d("User description descriptor read request");
                byte[] returnValue =  "Controls you AndrIoT device".getBytes(Charset.forName("UTF-8"));
                returnValue = Arrays.copyOfRange(returnValue, offset, returnValue.length);
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, returnValue);
            } else {
                Timber.w("Unknown descriptor read request");
                bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
            }
        }

        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            if (DESCRIPTOR_CONFIG.equals(descriptor.getUuid())) {
                if (Arrays.equals(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE, value)) {
                    Timber.d("Subscribe device to notifications: " + device);
                    registeredDevices.add(device);
                } else if (Arrays.equals(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE, value)) {
                    Timber.d("Unsubscribe device from notifications: " + device);
                    registeredDevices.remove(device);
                }

                if (responseNeeded) {
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, 0, null);
                }
            } else {
                Timber.w("Unknown descriptor write request");
                if (responseNeeded) {
                    bluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_FAILURE, 0, null);
                }
            }
        }
    };

    public GattServer(Context context) {

        appContext = context.getApplicationContext();
        bluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        context.registerReceiver(bluetoothReceiver, filter);
        if (!bluetoothAdapter.isEnabled()) {
            Timber.d("Bluetooth is currently disabled... enabling");
            bluetoothAdapter.enable();
        }/* else if (!bluetoothAdapter.isMultipleAdvertisementSupported()) {
            Timber.e("BLE advertising not supported on this device");
        }*/ else {
            Timber.d("Bluetooth enabled... starting services");
            startAdvertising();
            startServer();
        }
    }

    private void startAdvertising() {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothAdapter.setName("AndrIoT");
        bluetoothLeAdvertiser = bluetoothAdapter.getBluetoothLeAdvertiser();
        if (bluetoothLeAdvertiser == null) {
            Timber.w("Failed to create advertiser");
            return;
        }

        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM)
                .setConnectable(true)
                .build();

        byte[] serviceData = null;
        try {
            serviceData = buildServiceData();
        } catch (IOException e) {
            Timber.e(e);
        }

        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceData(EDDYSTONE_SERVICE_UUID, serviceData)
                .addServiceUuid(EDDYSTONE_SERVICE_UUID)
                .setIncludeTxPowerLevel(false)
                .setIncludeDeviceName(false)
                .build();

        bluetoothLeAdvertiser
                .startAdvertising(settings, data, advertiseCallback);
    }

    private void stopAdvertising() {
        if (bluetoothLeAdvertiser == null) {
            return;
        }
        bluetoothLeAdvertiser.stopAdvertising(advertiseCallback);
    }

    private void startServer() {
        bluetoothGattServer = bluetoothManager.openGattServer(appContext, mGattServerCallback);
        if (bluetoothGattServer == null) {
            Timber.w("Unable to create GATT server");
            return;
        }
        bluetoothGattServer.addService(createService());
    }

    private void stopServer() {
        if (bluetoothGattServer == null) {
            return;
        }
        bluetoothGattServer.close();
    }

    private BluetoothGattService createService() {
        BluetoothGattService service = new BluetoothGattService(SERVICE_UUID, BluetoothGattService.SERVICE_TYPE_PRIMARY);

        BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(CHARACTERISTIC_UUID,
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PERMISSION_WRITE);

        BluetoothGattDescriptor characteristicConfig = new BluetoothGattDescriptor(DESCRIPTOR_CONFIG,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
        characteristic.addDescriptor(characteristicConfig);

        BluetoothGattDescriptor characteristicDescription = new BluetoothGattDescriptor(DESCRIPTOR_USER_DESC,
                BluetoothGattDescriptor.PERMISSION_READ);
        characteristic.addDescriptor(characteristicDescription);

        service.addCharacteristic(characteristic);

        return service;
    }

    private void notifyRegisteredDevices() {
        if (registeredDevices.isEmpty()) {
            Timber.d("No subscribers registered");
            return;
        }

        Timber.d("Sending update to " + registeredDevices.size() + " subscribers");
        for (BluetoothDevice device : registeredDevices) {
            BluetoothGattCharacteristic characteristic = bluetoothGattServer
                    .getService(SERVICE_UUID)
                    .getCharacteristic(CHARACTERISTIC_UUID);
            byte[] value = btListener.onReadRequested();
            characteristic.setValue(value);
            bluetoothGattServer.notifyCharacteristicChanged(device, characteristic, false);
        }
    }

    private byte[] toByteArray(String hexString) {
        // hexString guaranteed valid.
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    private String randomHexString(int length) {
        byte[] buf = new byte[length];
        new Random().nextBytes(buf);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < length; i++) {
            stringBuilder.append(String.format("%02X", buf[i]));
        }
        return stringBuilder.toString();
    }

    private byte[] buildServiceData() throws IOException {
        byte txPower = (byte) -26;
        byte[] namespaceBytes = toByteArray(NAMESPACE_ID);
        byte[] instanceBytes = toByteArray(INSTANCE_ID); //randomHexString(6));
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        os.write(new byte[]{0x00, txPower}); // FRAME_TYPE_UID
        os.write(namespaceBytes);
        os.write(instanceBytes);
        return os.toByteArray();
    }

    public void destroyInstance() {
        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter.isEnabled()) {
            stopServer();
            stopAdvertising();
        }

        appContext.unregisterReceiver(bluetoothReceiver);
        btListener = null;
    }
}
