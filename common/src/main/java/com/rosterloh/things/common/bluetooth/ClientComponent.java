package com.rosterloh.things.common.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.location.LocationManager;
import android.support.annotation.Nullable;

import dagger.Binds;
import dagger.Component;
import dagger.Module;
import dagger.Provides;

@ClientScope
@Component(modules = {ClientComponent.ClientModule.class, ClientComponent.ClientModuleBinder.class})
public interface ClientComponent {

    @Module
    class ClientModule {

        private final Context context;

        public ClientModule(Context context) {
            this.context = context;
        }

        @Provides
        Context provideApplicationContext() {
            return context;
        }

        @Provides
        BluetoothManager provideBluetoothManager() {
            return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }

        @Provides
        @Nullable
        static BluetoothAdapter provideBluetoothAdapter() {
            return BluetoothAdapter.getDefaultAdapter();
        }

        @Provides
        LocationManager provideLocationManager() {
            return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        }
    }

    @Module
    abstract class ClientModuleBinder {

        @Binds
        @ClientScope
        abstract BleClient bindBleClient(BleClientImpl bleClient);
    }

    BleClient bleClient();
}
