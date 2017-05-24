package com.rosterloh.andriot.vo;

import android.arch.persistence.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = "projectId")
public class Settings {

    @SerializedName("project_id")
    public final String projectId;
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
}
