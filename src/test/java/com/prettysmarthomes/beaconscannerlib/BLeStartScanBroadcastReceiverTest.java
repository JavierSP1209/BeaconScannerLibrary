package com.prettysmarthomes.beaconscannerlib;

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
import static org.mockito.Mockito.mock;

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

    Intent testIntent = new Intent();
    testIntent.putExtra("MYPARAM", 20);
    ScanParameters expectedScanParams = mock(ScanParameters.class);
    testIntent.putExtra("OtherParam", expectedScanParams);
    subject.onReceive(RuntimeEnvironment.application, testIntent);

    Intent serviceIntent = shadowApplication.peekNextStartedService();
    assertThat("Service not started", serviceIntent, is(notNullValue()));
    assertThat("Expected the BLeScanService service to be invoked",
        BLeScanService.class.getCanonicalName(),
        is(serviceIntent.getComponent().getClassName()));
    assertThat((ScanParameters) serviceIntent.getParcelableExtra("OtherParam"), is(expectedScanParams));
    assertThat(serviceIntent.getIntExtra("MYPARAM", 0), is(20));
  }
}