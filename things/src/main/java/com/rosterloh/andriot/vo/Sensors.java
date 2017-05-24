package com.rosterloh.andriot.vo;

public class Sensors {

    public final Float temperature;
    public final Float humidity;
    public final String wifi_name;
    public final String wifi_ip;
    public final String eth_ip;

    public Sensors(Float temperature, Float humdity, String wifi_name, String wifi_ip, String eth_ip) {
        this.temperature = temperature;
        this.humidity = humdity;
        this.wifi_name = wifi_name;
        this.wifi_ip = wifi_ip;
        this.eth_ip = eth_ip;
    }
}
