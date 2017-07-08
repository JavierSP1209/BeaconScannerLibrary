package com.prettysmarthomes.beaconscannerlib.di;

import android.bluetooth.BluetoothAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

import static org.mockito.Mockito.mock;

/**
 * Base module for BLeScanService
 */
@Module(includes = BleScanServiceBaseModule.class)
public final class BleScanServiceTestModule {

  @Provides
  @Singleton
  static BluetoothAdapter providesBluetoothAdapter() {
    return mock(BluetoothAdapter.class);
  }

  @Provides
  @Singleton
  static BluetoothLeScannerCompat providesBluetoothLeScannerCompat() {
    return mock(BluetoothLeScannerCompat.class);
  }
}
