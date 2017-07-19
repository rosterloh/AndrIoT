package com.rosterloh.andriot.db;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

import java.util.Locale;
import java.util.UUID;

@Entity(tableName = "cloud_settings")
public class CloudSettings {

    public static final String DEFAULT_PROJECT_ID = "andriot-b80e9";
    public static final String DEFAULT_REGISTRY_ID = "AndrIot";
    public static final String DEFAULT_CLOUD_REGION = "europe-west2";
    public static final String DEFAULT_BRIDGE_HOSTNAME = "mqtt.googleapis.com";
    public static final short DEFAULT_BRIDGE_PORT = 443;
    public static final String UNUSED_ACCOUNT_NAME = "unused";

    /**
     * Notice that for CloudIoT the topic for telemetry events needs to have the format below.
     * As described <a href="https://cloud.google.com/iot/docs/protocol_bridge_guide#telemetry_events">in docs</a>,
     * messages published to a topic with this format are augmented with extra attributes and
     * forwarded to the Pub/Sub topic specified in the registry resource.
     */
    private static final String MQTT_TOPIC_FORMAT = "/devices/%s/events";
    private static final String MQTT_CLIENT_ID_FORMAT = "projects/%s/locations/%s/registries/%s/devices/%s";
    private static final String BROKER_URL_FORMAT = "ssl://%s:%d";

    @PrimaryKey
    @ColumnInfo(name = "project_id")
    private final String mProjectId;

    @ColumnInfo(name = "registry_id")
    private final String mRegistryId;

    @ColumnInfo(name = "device_id")
    private final String mDeviceId;

    @ColumnInfo(name = "cloud_region")
    private final String mCloudRegion;

    @ColumnInfo(name = "bridge_hostname")
    private final String mBridgeHostname;

    @ColumnInfo(name = "bridge_port")
    private final short mBridgePort;

    @Ignore
    public CloudSettings() {
        mProjectId = DEFAULT_PROJECT_ID;
        mRegistryId = DEFAULT_REGISTRY_ID;
        mDeviceId = UUID.randomUUID().toString();
        mCloudRegion = DEFAULT_CLOUD_REGION;
        mBridgeHostname = DEFAULT_BRIDGE_HOSTNAME;
        mBridgePort = DEFAULT_BRIDGE_PORT;
    }

    public CloudSettings(String projectId, String registryId, String deviceId, String cloudRegion,
                         String bridgeHostname, short bridgePort) {
        mProjectId = projectId;
        mRegistryId = registryId;
        mDeviceId = deviceId;
        mCloudRegion = cloudRegion;
        mBridgeHostname = bridgeHostname;
        mBridgePort = bridgePort;
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

    @Override
    public String toString() {
        return "CloudSettings{"
                + "project_id= " + mProjectId
                + "registry_id= " + mRegistryId
                + "device_id= " + mDeviceId
                + "cloud_region= " + mCloudRegion
                + "bridge_hostname= " + mBridgeHostname
                + "bridge_port= " + mBridgePort
                + '}';
    }
}
