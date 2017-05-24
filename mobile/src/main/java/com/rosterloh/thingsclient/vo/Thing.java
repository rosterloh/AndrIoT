package com.rosterloh.thingsclient.vo;

import android.arch.persistence.room.Entity;

import com.google.gson.annotations.SerializedName;

@Entity(primaryKeys = "device")
public class Thing {

    @SerializedName("address")
    public final String address;
    @SerializedName("name")
    public final String name;

    public Thing(String address, String name) {
        this.address = address;
        this.name = name;
    }
}
