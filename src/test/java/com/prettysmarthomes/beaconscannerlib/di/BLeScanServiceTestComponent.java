package com.prettysmarthomes.beaconscannerlib.di;

import android.bluetooth.BluetoothAdapter;

import javax.inject.Singleton;

import dagger.Component;
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;

/**
 * Test component for dagger 2 for BLeScanService`
 */
@Singleton
@Component(modules = BleScanServiceTestModule.class)
public interface BLeScanServiceTestComponent extends BLeScanServiceComponent {

  BluetoothAdapter getBluetoothAdapter();

  BluetoothLeScannerCompat getBluetoothLeScannerCompat();
}
