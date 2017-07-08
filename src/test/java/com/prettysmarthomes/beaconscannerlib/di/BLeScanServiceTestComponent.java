package com.prettysmarthomes.beaconscannerlib.di;

import com.prettysmarthomes.beaconscannerlib.BLeScanService;
import com.prettysmarthomes.beaconscannerlib.BLeScanServiceTest;

import dagger.Component;

/**
 * Test component for dagger 2 for BLeScanService`
 */
@Component(modules = BleScanServiceTestModule.class)
public interface BLeScanServiceTestComponent extends BLeScanServiceComponent{
  void inject(BLeScanServiceTest test);
}
