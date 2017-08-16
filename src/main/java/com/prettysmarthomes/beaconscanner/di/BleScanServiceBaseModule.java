package com.prettysmarthomes.beaconscanner.di;

import android.app.Service;
import android.content.Context;
import android.support.annotation.NonNull;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;

/**
 * Base module for BaseScanService includes the providers that will not be replaced for testing
 */
@Module(includes = BleScanServiceBaseModule.Bindings.class)
public class BleScanServiceBaseModule {
  @NonNull
  private final Service service;

  @Module
  public interface Bindings {
    @Binds
    Context bindContext(Service service);
  }

  public BleScanServiceBaseModule(@NonNull Service service) {
    this.service = service;
  }

  @Provides
  Service provideService() {
    return service;
  }
}
