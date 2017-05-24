package com.rosterloh.things.common.bluetooth;

import android.content.Context;
import android.support.annotation.NonNull;

public abstract class BleClient {

    public static BleClient create(@NonNull Context context) {
        return DaggerClientComponent
                .builder()
                .clientModule(new ClientComponent.ClientModule(context))
                .build()
                .bleClient();
    }
}
