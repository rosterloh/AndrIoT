package com.rosterloh.andriot.vo;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.util.Locale;

import static com.rosterloh.andriot.vo.Settings.TABLE_NAME;

@Entity(tableName = TABLE_NAME)
public class Settings {

    public static final String TABLE_NAME = "settings";

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

    @SerializedName("project_id")
    public final @PrimaryKey String projectId;
    @SerializedName("registry_id")
    public final String registryId;
    @SerializedName("device_id")
    public final String deviceId;
    @SerializedName("cloud_region")
    public final String cloudRegion;
    @SerializedName("bridge_hostname")
    public final String bridgeHostname;
    @SerializedName("bridge_port")
    public final short bridgePort;

    public Settings(String projectId, String registryId, String deviceId, String cloudRegion,
                    String bridgeHostname, short bridgePort) {
        this.projectId = projectId;
        this.registryId = registryId;
        this.deviceId = deviceId;
        this.cloudRegion = cloudRegion;
        this.bridgeHostname = bridgeHostname;
        this.bridgePort = bridgePort;
    }

    public String getBrokerUrl() {
        return String.format(Locale.getDefault(), BROKER_URL_FORMAT, bridgeHostname, bridgePort);
    }

    public String getClientId() {
        return String.format(Locale.getDefault(), MQTT_CLIENT_ID_FORMAT,
                projectId, cloudRegion, registryId, deviceId);
    }

    public String getTopicName() {
        return String.format(Locale.getDefault(), MQTT_TOPIC_FORMAT, deviceId);
    }

    @Override
    public String toString() {
        return "Settings{" +
                "project_id= " + projectId +
                "registry_id= " + registryId +
                "device_id= " + deviceId +
                "cloud_region= " + cloudRegion +
                "bridge_hostname= " + bridgeHostname +
                "bridge_port= " + bridgePort +
                '}';
    }
}
