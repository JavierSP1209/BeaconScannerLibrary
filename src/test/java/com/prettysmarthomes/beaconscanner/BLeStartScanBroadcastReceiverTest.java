package com.prettysmarthomes.beaconscanner;

import android.content.Intent;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeStartScanBroadcastReceiverTest {

  private BLeStartScanBroadcastReceiver subject;
  private ShadowApplication shadowApplication;

  @Before
  public void setUp() throws Exception {
    subject = new BLeStartScanBroadcastReceiver();
    shadowApplication = ShadowApplication.getInstance();
  }

  @Test
  public void onReceive_shouldStartService() throws Exception {

    byte[] filter = {0x01, 0x02, 0x03};
    long interval = 2L;
    long period = 3L;
    int manufacturerId = 1;
    Intent testIntent = new Intent();
    testIntent.putExtra("EXTRA_FILTER", filter);
    testIntent.putExtra("EXTRA_MANUFACTURER_ID", manufacturerId);
    testIntent.putExtra("EXTRA_SCAN_INTERVAL", interval);
    testIntent.putExtra("EXTRA_SCAN_PERIOD", period);
    subject.onReceive(RuntimeEnvironment.application, testIntent);

    Intent serviceIntent = shadowApplication.peekNextStartedService();
    assertThat("Service not started", serviceIntent, is(notNullValue()));
    assertThat("Expected the BLeScanService service to be invoked",
        BLeScanService.class.getCanonicalName(),
        is(serviceIntent.getComponent().getClassName()));
    ScanParameters scanParams = serviceIntent.getParcelableExtra("com.prettysmarthomes.beaconscanner.SCAN_PARAMS");
    assertThat(scanParams.getFilterUUIDData(), is(filter));
    assertThat(scanParams.getManufacturerId(), is(manufacturerId));
    assertThat(scanParams.getScanPeriod(), is(period));
    assertThat(scanParams.getScanInterval(), is(interval));
  }
}