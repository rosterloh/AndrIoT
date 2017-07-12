package com.rosterloh.andriot.cloud;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;

import timber.log.Timber;

public final class AndrIotOptions {

    private static final String DEFAULT_BRIDGE_HOSTNAME = "mqtt.googleapis.com";
    private static final short DEFAULT_BRIDGE_PORT = 443;

    /**
     * Notice that for CloudIoT the topic for telemetry events needs to have the format below.
     * As described <a href="https://cloud.google.com/iot/docs/protocol_bridge_guide#telemetry_events">in docs</a>,
     * messages published to a topic with this format are augmented with extra attributes and
     * forwarded to the Pub/Sub topic specified in the registry resource.
     */
    private static final String MQTT_TOPIC_FORMAT = "/devices/%s/events";
    private static final String MQTT_CLIENT_ID_FORMAT
            = "projects/%s/locations/%s/registries/%s/devices/%s";
    private static final String BROKER_URL_FORMAT = "ssl://%s:%d";

    private String mProjectId;
    private String mRegistryId;
    private String mDeviceId;
    private String mCloudRegion;
    private String mBridgeHostname = DEFAULT_BRIDGE_HOSTNAME;
    private short mBridgePort = DEFAULT_BRIDGE_PORT;


    public String getBrokerUrl() {
        return String.format(Locale.getDefault(), BROKER_URL_FORMAT, mBridgeHostname, mBridgePort);
    }

    public String getClientId() {
        return String.format(Locale.getDefault(), MQTT_CLIENT_ID_FORMAT,
                mProjectId, mCloudRegion, mRegistryId, mDeviceId);
    }

    public String getTopicName() {
        return String.format(Locale.getDefault(), MQTT_TOPIC_FORMAT, mDeviceId);
    }

    public String getProjectId() {
        return mProjectId;
    }

    public String getRegistryId() {
        return mRegistryId;
    }

    public String getDeviceId() {
        return mDeviceId;
    }

    public String getCloudRegion() {
        return mCloudRegion;
    }

    public String getBridgeHostname() {
        return mBridgeHostname;
    }

    public short getBridgePort() {
        return mBridgePort;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(mProjectId)
                && !TextUtils.isEmpty(mRegistryId)
                && !TextUtils.isEmpty(mDeviceId)
                && !TextUtils.isEmpty(mCloudRegion)
                && !TextUtils.isEmpty(mBridgeHostname);
    }

    public void saveToPreferences(SharedPreferences pref) {
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("project_id", mProjectId);
        editor.putString("registry_id", mRegistryId);
        editor.putString("device_id", mDeviceId);
        editor.putString("cloud_region", mCloudRegion);
        editor.putString("mqtt_bridge_hostname", mBridgeHostname);
        editor.putInt("mqtt_bridge_port", mBridgePort);
        editor.apply();
    }

    public static AndrIotOptions from(SharedPreferences pref) {
        try {
            AndrIotOptions options = new AndrIotOptions();
            options.mProjectId = pref.getString("project_id", null);
            options.mRegistryId = pref.getString("registry_id", null);
            options.mDeviceId = pref.getString("device_id", null);
            options.mCloudRegion = pref.getString("cloud_region", null);
            options.mBridgeHostname = pref.getString("mqtt_bridge_hostname",
                    DEFAULT_BRIDGE_HOSTNAME);
            options.mBridgePort = (short) pref.getInt("mqtt_bridge_port", DEFAULT_BRIDGE_PORT);
            return options;
        } catch (Exception e) {
            throw new IllegalArgumentException("While processing configuration options", e);
        }
    }

    public static AndrIotOptions reconfigure(AndrIotOptions original, Bundle bundle) {
        try {
            HashSet<String> valid = new HashSet<>(Arrays.asList(new String[] {"project_id",
                    "registry_id", "device_id", "cloud_region", "mqtt_bridge_hostname",
                    "mqtt_bridge_port"}));
            valid.retainAll(bundle.keySet());
            Timber.i("Configuring options using the following intent extras: " + valid);
            AndrIotOptions result = new AndrIotOptions();
            result.mProjectId = bundle.getString("project_id", original.mProjectId);
            result.mRegistryId = bundle.getString("registry_id", original.mRegistryId);
            result.mDeviceId = bundle.getString("device_id", original.mDeviceId);
            result.mCloudRegion = bundle.getString("cloud_region", original.mCloudRegion);
            result.mBridgeHostname = bundle.getString("mqtt_bridge_hostname",
                    original.mBridgeHostname);
            result.mBridgePort = (short) bundle.getInt("mqtt_bridge_port", original.mBridgePort);
            return result;
        } catch (Exception e) {
            throw new IllegalArgumentException("While processing configuration options", e);
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AndrIotOptions)) {
            return false;
        }
        AndrIotOptions o = (AndrIotOptions) obj;
        return TextUtils.equals(mProjectId , o.mProjectId)
                && TextUtils.equals(mRegistryId, o.mRegistryId)
                && TextUtils.equals(mDeviceId, o.mDeviceId)
                && TextUtils.equals(mCloudRegion, o.mCloudRegion)
                && TextUtils.equals(mBridgeHostname, o.mBridgeHostname)
                && o.mBridgePort == mBridgePort;
    }
}
