package com.prettysmarthomes.beaconscannerlib.di;

import com.prettysmarthomes.beaconscannerlib.BLeScanService;

import dagger.Component;

/**
 * Base component for dagger 2 for BLeScanService`
 */
@Component(modules = BleScanServiceModule.class)
public interface BLeScanServiceComponent {
  void inject(BLeScanService service);
}
