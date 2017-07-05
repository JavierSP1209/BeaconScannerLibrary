package com.prettysmarthomes.beaconscannerlib.di;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Base module for BLeScanService
 */
@Module(includes = BleScanServiceModule.Bindings.class)
public final class BleScanServiceModule {

  @NonNull
  private final Service service;

  @Module
  public interface Bindings {
    @Binds
    Context bindContext(Service service);
  }

  public BleScanServiceModule(@NonNull Service service) {
    this.service = service;
  }

  @Provides
  Service provideService() {
    return service;
  }

  @Provides
  static BluetoothAdapter providesBluetoothAdapter() {
    return BluetoothAdapter.getDefaultAdapter();
  }
}
