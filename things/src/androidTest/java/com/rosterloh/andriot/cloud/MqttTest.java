package com.rosterloh.andriot.cloud;

import android.support.test.runner.AndroidJUnit4;

import org.eclipse.paho.client.mqttv3.IMqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class MqttTest {

    @Test
    public void testConnect() throws Exception {
        IMqttAsyncClient mqttClient = null;
        try {
            mqttClient = new MqttAsyncClient("192.168.86.77:1883", "testConnect");
        } catch (Exception e) {
            Assert.fail("testConnect failed: " + e);
        } finally {
            if (mqttClient != null) {
                mqttClient.close();
            }
        }
    }
}
