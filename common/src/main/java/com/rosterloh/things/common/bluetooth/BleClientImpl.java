package com.rosterloh.things.common.bluetooth;

import javax.inject.Inject;

class BleClientImpl extends BleClient {

    private final BleAdapterWrapper bleAdapterWrapper;

    @Inject
    BleClientImpl(BleAdapterWrapper bleAdapterWrapper) {
        this.bleAdapterWrapper = bleAdapterWrapper;
    }
}
