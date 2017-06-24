package com.psh.beaconscannerlib.provider;

import android.bluetooth.BluetoothAdapter;


public class BluetoothAdapterProvider {

  public BluetoothAdapter getInstance() {
    return BluetoothAdapter.getDefaultAdapter();
  }
}
