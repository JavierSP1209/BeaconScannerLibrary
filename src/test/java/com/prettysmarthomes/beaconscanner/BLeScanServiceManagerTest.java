package com.prettysmarthomes.beaconscanner;

import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowPendingIntent;

import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeScanServiceManagerTest {

  private static final long INTERVAL = 20000L;
  private static final long PERIOD = 20000L;
  private static final byte[] FILTER = {0x01, 0x02, 0x03};
  private static final int MANUFACTURER_ID = 1;
  @Mock
  private ScanParameters scanParameters;
  private ShadowAlarmManager shadowAlarmManager;
  private BLeScanServiceManager subject;

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
    subject = new BLeScanServiceManager();
    when(scanParameters.getScanInterval()).thenReturn(INTERVAL);
    when(scanParameters.getScanPeriod()).thenReturn(PERIOD);
    when(scanParameters.getManufacturerId()).thenReturn(MANUFACTURER_ID);
    when(scanParameters.getFilterUUIDData()).thenReturn(FILTER);
    AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(Context.ALARM_SERVICE);
    shadowAlarmManager = shadowOf(alarmManager);
  }

  @Test
  public void startScanAlarm_shouldScheduleAlarm() {
    Assert.assertNull("Previous alarm exists", shadowAlarmManager.getNextScheduledAlarm());
    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    Assert.assertNotNull("Alarm not scheduled", repeatingAlarm);
  }

  @Test
  public void startScanAlarm_shouldScheduleAlarmEachInterval() {
    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    long startTime = System.currentTimeMillis();
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(AlarmManager.RTC, is(repeatingAlarm.type));
    long expectedTriggerTime = startTime + INTERVAL;
    long timeDifference = expectedTriggerTime - repeatingAlarm.triggerAtTime;
    assertThat(timeDifference, lessThan(5L));
  }

  @Test
  public void startScanAlarm_shouldScheduleOnlyOneTime() throws Exception {
    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    subject.startScanService(RuntimeEnvironment.application, scanParameters);

    assertThat(1, is(shadowAlarmManager.getScheduledAlarms().size()));
  }

  @Test
  public void startScanAlarm_shouldTriggerBroadcastReceiverWhenTimeElapsed()
      throws Exception {
    Intent expectedIntent = new Intent(RuntimeEnvironment.application, BLeStartScanBroadcastReceiver.class);

    subject.startScanService(RuntimeEnvironment.application, scanParameters);

    ShadowAlarmManager.ScheduledAlarm scheduledAlarm = shadowAlarmManager.getNextScheduledAlarm();
    ShadowPendingIntent shadowPendingIntent = shadowOf(scheduledAlarm.operation);
    assertThat(shadowPendingIntent.isBroadcastIntent(), is(true));
    assertThat(1, is(shadowPendingIntent.getSavedIntents().length));
    assertThat(expectedIntent.getComponent(), is(
        shadowPendingIntent.getSavedIntents()[0].getComponent()));
  }

  @Test
  public void startScanAlarm_setScanParameterExtras() {

    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    ShadowPendingIntent pendingIntent = shadowOf(repeatingAlarm.operation);
    Intent intent = pendingIntent.getSavedIntent();
    assertThat(intent.getLongExtra("EXTRA_SCAN_PERIOD", 0L), is(PERIOD));
    assertThat(intent.getLongExtra("EXTRA_SCAN_INTERVAL", 0L), is(INTERVAL));
    assertThat(intent.getByteArrayExtra("EXTRA_FILTER"), is(FILTER));
    assertThat(intent.getIntExtra("EXTRA_MANUFACTURER_ID", 0), is(MANUFACTURER_ID));
  }

  @Test
  public void cancelSHealthSyncAlarm_shouldRemoveAlarm() {
    subject.startScanService(RuntimeEnvironment.application, scanParameters);
    subject.cancelScanService(RuntimeEnvironment.application);
    ShadowAlarmManager.ScheduledAlarm repeatingAlarm = shadowAlarmManager.getNextScheduledAlarm();
    Assert.assertNull("Alarm scheduled", repeatingAlarm);
  }
}