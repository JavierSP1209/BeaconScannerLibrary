package com.prettysmarthomes.beaconscanner.di;

import com.prettysmarthomes.beaconscanner.BLeScanService;

import dagger.Component;

/**
 * Base component for dagger 2 for BLeScanService
 */
@Component(modules = BleScanServiceModule.class)
public interface BLeScanServiceComponent {
  void inject(BLeScanService service);
}
