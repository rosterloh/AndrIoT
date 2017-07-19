package com.rosterloh.andriot.common;

import android.location.Location;

import com.rosterloh.andriot.common.nearby.MessagePayload;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static junit.framework.Assert.*;
import static org.mockito.Mockito.mock;

public class NearbyMessageTest {

    private Location mLocation;

    @Rule
    public ExpectedException mExpectedException = ExpectedException.none();

    @Test
    public void testLocationMessage() throws Exception {
        mLocation = mock(Location.class);
        MessagePayload msg = new MessagePayload(mLocation);
        //System.out.println(msg.toString());
        byte[] tx = MessagePayload.marshall(msg);
        MessagePayload rx = MessagePayload.unmarshall(tx, MessagePayload.CREATOR);
        assertEquals(rx.getType(), msg.getType());
    }

    @Test
    public void testMessagePayloadAsLocationMessage() {

    }
}