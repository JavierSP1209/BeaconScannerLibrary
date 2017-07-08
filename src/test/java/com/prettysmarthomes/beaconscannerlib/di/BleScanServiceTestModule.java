package com.prettysmarthomes.beaconscannerlib.di;

import android.bluetooth.BluetoothAdapter;

import dagger.Module;
import dagger.Provides;

import static org.mockito.Mockito.mock;

/**
 * Base module for BLeScanService
 */
@Module(includes = {
    BleScanServiceBaseModule.class,
    BleScanServiceBaseModule.Bindings.class})
public final class BleScanServiceTestModule {

  @Provides
  static BluetoothAdapter providesBluetoothAdapter() {
    return mock(BluetoothAdapter.class);
  }
}
