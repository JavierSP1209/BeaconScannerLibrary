package com.prettysmarthomes.beaconscannerlib.provider;

import android.bluetooth.BluetoothAdapter;


public class BluetoothAdapterProvider {

  public BluetoothAdapter getInstance() {
    return BluetoothAdapter.getDefaultAdapter();
  }
}
