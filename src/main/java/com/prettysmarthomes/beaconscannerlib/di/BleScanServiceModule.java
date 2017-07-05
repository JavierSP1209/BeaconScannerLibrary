package com.prettysmarthomes.beaconscannerlib.di;

import android.bluetooth.BluetoothAdapter;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Base module for BLeScanService
 */
@Module
public final class BleScanServiceModule {

  @Provides
  @Singleton
  static BluetoothAdapter providesBluetoothAdapter() {
    return BluetoothAdapter.getDefaultAdapter();
  }
}
