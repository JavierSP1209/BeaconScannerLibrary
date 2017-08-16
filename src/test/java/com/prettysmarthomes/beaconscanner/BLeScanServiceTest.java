package com.prettysmarthomes.beaconscanner;

import android.app.AlarmManager;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.prettysmarthomes.beaconscanner.di.BLeScanServiceTestComponent;
import com.prettysmarthomes.beaconscanner.di.BleScanServiceBaseModule;
import com.prettysmarthomes.beaconscanner.di.DaggerBLeScanServiceTestComponent;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.IntentServiceController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowAlarmManager;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowLooper;

import java.util.List;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class BLeScanServiceTest {

  private static final long SCAN_PERIOD = 13435L;
  private static final long SCAN_INTERVAL = 1000L;
  private static final byte[] FILTER_DATA = {0, 0, -71, 64, 127, 48, -11, -8, 70, 110, -81, -7, 37, 85, 107, 87, -2, 109, 0, 0, 0, 0, 0};
  private static final int MY_MANUFACTURER_ID = 0x20;

  @Mock
  private Handler mockStopScanHandler;
  @Mock
  private ScanParameters scanParameters;
  private BluetoothLeScannerCompat mockScannerCompat;
  private BluetoothAdapter mockBluetoothAdapter;
  private BLeScanServiceManager mockBLeScanServiceManager;
  @Captor
  private ArgumentCaptor<List<ScanFilter>> scanFilterCaptor;
  private BLeScanServiceMock scanService;
  private Intent serviceIntent;
  private ShadowAlarmManager shadowAlarmManager;
  private boolean broadcastSent;
  private IntentServiceController<BLeScanServiceMock> controller;

  @Before
  public void setUp() throws Exception {
    ShadowLog.stream = System.out;
    // create and injects mocks into object annotated with @InjectMocks
    MockitoAnnotations.initMocks(this);
    serviceIntent = new Intent(RuntimeEnvironment.application, BLeScanService.class);
    when(scanParameters.getScanPeriod()).thenReturn(SCAN_PERIOD);
    when(scanParameters.getScanInterval()).thenReturn(SCAN_INTERVAL);
    when(scanParameters.getFilterUUIDData()).thenReturn(FILTER_DATA);
    when(scanParameters.getManufacturerId()).thenReturn(MY_MANUFACTURER_ID);
    serviceIntent.putExtra("com.prettysmarthomes.beaconscanner.SCAN_PARAMS", scanParameters);

    AlarmManager alarmManager = (AlarmManager) RuntimeEnvironment.application.getSystemService(
        Context.ALARM_SERVICE);
    shadowAlarmManager = shadowOf(alarmManager);

    broadcastSent = false;
    controller = Robolectric.buildIntentService(BLeScanServiceMock.class);
    scanService = controller.create().get();
    mockBluetoothAdapter = scanService.testComponent.getBluetoothAdapter();
    mockScannerCompat = scanService.testComponent.getBluetoothLeScannerCompat();
    mockBLeScanServiceManager = scanService.testComponent.getScanAlarmManager();
    when(mockBluetoothAdapter.isEnabled()).thenReturn(true);
  }

  @After
  public void tearDown() {
    controller.destroy();
  }

  @Test
  public void onHandleIntent_shouldStartScanning() {
    ArgumentCaptor<ScanSettings> settingsArgumentCaptor = ArgumentCaptor.forClass(
        ScanSettings.class);
    scanService.onHandleIntent(serviceIntent);

    verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
        settingsArgumentCaptor.capture(),
        any(ScanResultCallback.class));
    verify(mockBLeScanServiceManager).startScanService(RuntimeEnvironment.application, scanParameters);

    ScanSettings actualSettings = settingsArgumentCaptor.getValue();
    assertThat(actualSettings.getScanMode(), is(equalTo(ScanSettings.SCAN_MODE_BALANCED)));
    assertThat(actualSettings.getReportDelayMillis(), is(equalTo(1000L)));
    assertThat(actualSettings.getUseHardwareBatchingIfSupported(), is(false));

    byte[] mask = new byte[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};

    // Add data array to filters
    List<ScanFilter> actualFilters = scanFilterCaptor.getValue();
    assertThat(actualFilters.size(), is(1));
    ScanFilter actualFilter = actualFilters.get(0);
    assertThat(actualFilter.getManufacturerDataMask(), is(mask));
    assertThat(actualFilter.getManufacturerData(), is(FILTER_DATA));
    assertThat(actualFilter.getManufacturerId(), is(MY_MANUFACTURER_ID));
  }

  @Test(expected = IllegalStateException.class)
  public void onHandleIntent_whenNullScanParams_shouldThrowIllegalStateException() {
    scanService.onHandleIntent(new Intent());
  }

  @Test
  public void onHandleIntent_invalidManufacturerId_doNotSetManufacturerData() {
    //Invalid ManufacturerId means that the value is negative
    when(scanParameters.getManufacturerId()).thenReturn(-2);
    scanService.onHandleIntent(serviceIntent);
    verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
        any(ScanSettings.class),
        any(ScanResultCallback.class));
    ScanFilter actualFilter = scanFilterCaptor.getValue().get(0);
    assertThat(actualFilter.getManufacturerId(), is(-1));
  }


  @Test
  public void onHandleIntent_nullFilterData_shouldSetNullManufacturerDataInFilter() {
    //Invalid FilterData means null when mask is not null or length of mask and data is different
    when(scanParameters.getFilterUUIDData()).thenReturn(null);
    scanService.onHandleIntent(serviceIntent);
    verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
        any(ScanSettings.class),
        any(ScanResultCallback.class));

    ScanFilter actualFilter = scanFilterCaptor.getValue().get(0);
    assertThat(actualFilter.getManufacturerId(), is(MY_MANUFACTURER_ID));
    assertThat(actualFilter.getManufacturerData(), is(nullValue()));
    assertThat(actualFilter.getManufacturerDataMask(), is(nullValue()));
  }

  @Test
  public void onHandleIntent_invalidFilterData_shouldSetNullManufacturerDataInFilter() {
    //Invalid FilterData means null when mask is not null or length of mask and data is different
    when(scanParameters.getFilterUUIDData()).thenReturn(new byte[]{0, 0, 1, 2, 3, 4, 0});
    scanService.onHandleIntent(serviceIntent);
    verify(mockScannerCompat).startScan(scanFilterCaptor.capture(),
        any(ScanSettings.class),
        any(ScanResultCallback.class));

    ScanFilter actualFilter = scanFilterCaptor.getValue().get(0);
    assertThat(actualFilter.getManufacturerId(), is(MY_MANUFACTURER_ID));
    assertThat(actualFilter.getManufacturerData(), is(nullValue()));
    assertThat(actualFilter.getManufacturerDataMask(), is(nullValue()));
  }

  @Test
  public void onHandleIntent_shouldSetScanPeriodAsPostDelay() {
    scanService.setStopScanHandler(mockStopScanHandler);
    scanService.onHandleIntent(serviceIntent);
    verify(mockStopScanHandler).postDelayed(any(Runnable.class), eq(SCAN_PERIOD));
  }

  @Test
  public void onHandleIntent_whenBLeIsNotEnabled_shouldNotScanNorRestartService() {
    when(mockBluetoothAdapter.isEnabled()).thenReturn(false);
    scanService.onHandleIntent(serviceIntent);

    ShadowAlarmManager.ScheduledAlarm alarm = shadowAlarmManager.getNextScheduledAlarm();
    assertThat(alarm, nullValue());
  }

  @Test
  public void onHandleIntent_sendStartLocalBroadcast() {
    registerLocalReceiver("com.prettysmarthomes.beaconscanner.SCAN_STARTED");
    scanService.onHandleIntent(serviceIntent);
    assertThat(broadcastSent, is(true));
  }

  @Test
  public void stopScanHandler_stopScansAfterPeriodFinish() {
    scanService.onHandleIntent(serviceIntent);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    verify(mockScannerCompat).stopScan(any(ScanResultCallback.class));
  }

  @Test
  public void stopScanHandler_sendLocalStopBroadcast() {
    registerLocalReceiver("com.prettysmarthomes.beaconscanner.SCAN_STOPPED");
    scanService.onHandleIntent(serviceIntent);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();
    assertThat(broadcastSent, is(true));
  }

  private void registerLocalReceiver(String action) {
    LocalBroadcastManager broadcastManager = LocalBroadcastManager.getInstance(
        RuntimeEnvironment.application);
    broadcastManager.registerReceiver(new BroadcastReceiver() {
      @Override
      public void onReceive(Context arg0, Intent intent) {
        broadcastSent = true;
      }
    }, new IntentFilter(action));
  }

  static class BLeScanServiceMock extends BLeScanService {
    private BLeScanServiceTestComponent testComponent;

    public BLeScanServiceMock(String name) {
      super();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
      super.onHandleIntent(intent);
    }

    @Override
    protected void injectMembers() {
      testComponent = DaggerBLeScanServiceTestComponent.builder()
          .bleScanServiceBaseModule(new BleScanServiceBaseModule(this))
          .build();
      testComponent.inject(this);
    }
  }
}