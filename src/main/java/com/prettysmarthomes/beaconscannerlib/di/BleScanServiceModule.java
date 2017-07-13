package com.prettysmarthomes.beaconscannerlib.di;

import android.bluetooth.BluetoothAdapter;

import com.prettysmarthomes.beaconscannerlib.ScanAlarmManager;

import dagger.Module;
import dagger.Provides;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

/**
 * Base module for BLeScanService
 */
@Module(includes = BleScanServiceBaseModule.class)
public final class BleScanServiceModule {

  @Provides
  static BluetoothAdapter providesBluetoothAdapter() {
    return BluetoothAdapter.getDefaultAdapter();
  }

  @Provides
  static BluetoothLeScannerCompat providesBluetoothLeScannerCompat() {
    return BluetoothLeScannerCompat.getScanner();
  }

  @Provides
  static ScanAlarmManager providesScanAlarmManager() {
    return new ScanAlarmManager();
  }
}
