package com.prettysmarthomes.beaconscannerlib;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;

import com.prettysmarthomes.beaconscannerlib.di.BleScanServiceModule;
import com.prettysmarthomes.beaconscannerlib.di.DaggerBLeScanServiceComponent;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat;
import no.nordicsemi.android.support.v18.scanner.ScanFilter;
import no.nordicsemi.android.support.v18.scanner.ScanSettings;

/**
 * BLe cycled scanner, this service will be restarted when the scan is finished unless BLe is not
 * enabled
 */
public class BLeScanService extends IntentService {

  private static final byte[] MASK = new byte[]{0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0};
  public static final String ACTION_BEACON_FOUND = "com.prettysmarthomes.beaconscannerlib.BEACON_FOUND";
  public static final String ACTION_SCAN_START = "com.prettysmarthomes.beaconscannerlib.SCAN_START";
  public static final String ACTION_SCAN_STOP = "com.prettysmarthomes.beaconscannerlib.SCAN_STOP";

  public static final String EXTRA_BEACON_CONTENT = "com.prettysmarthomes.beaconscannerlib.BEACON_CONTENT";
  public static final String EXTRA_SCAN_PERIOD = "com.prettysmarthomes.beaconscannerlib.SCAN_PERIOD";
  public static final String EXTRA_SCAN_INTERVAL = "com.prettysmarthomes.beaconscannerlib.SCAN_INTERVAL";
  public static final String EXTRA_FILTER_UUID = "com.prettysmarthomes.beaconscannerlib.FILTER_UUID";
  public static final String TAG = "BleScanService";

  private Handler stopScanHandler;
  private BluetoothLeScannerCompat scanner;
  private byte[] filterData;
  private long scanInterval;

  @Inject
  BluetoothAdapter adapter;
  @Inject
  CustomScanCallback scanCallback;

  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      scanner.stopScan(scanCallback);
      sendStateLocalBroadcast(ACTION_SCAN_STOP);
    }
  };
  private long scanPeriod;

  void setStopScanHandler(Handler stopScanHandler) {
    this.stopScanHandler = stopScanHandler;
  }

  void setScanner(BluetoothLeScannerCompat scanner) {
    this.scanner = scanner;
  }

  public BLeScanService() {
    super(TAG);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    DaggerBLeScanServiceComponent.builder()
        .bleScanServiceModule(new BleScanServiceModule(this))
        .build()
        .inject(this);
    stopScanHandler = new Handler();
    scanner = BluetoothLeScannerCompat.getScanner();
    scanCallback = new CustomScanCallback(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    filterData = intent.getByteArrayExtra(EXTRA_FILTER_UUID);
    scanPeriod = intent.getLongExtra(EXTRA_SCAN_PERIOD,
        ScanParameters.DEFAULT_BLE_SCAN_PERIOD_MS);
    scanInterval = intent.getLongExtra(EXTRA_SCAN_INTERVAL,
        ScanParameters.DEFAULT_BLE_SCAN_INTERVAL_MS);
    if (isBLeEnabled()) {
      startScan();
      restartService();
      stopScanHandler.postDelayed(serviceStarter, scanPeriod);
    }
  }

  private void startScan() {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(
            ScanParameters.SCAN_RESULTS_DELAY)
        .setUseHardwareBatchingIfSupported(false).build();
    List<ScanFilter> filters = new ArrayList<>();
    ScanFilter.Builder builder = new ScanFilter.Builder();
    if (filterData != null) {
      builder.setManufacturerData(89, filterData, MASK);
    }
    filters.add(builder.build());
    scanner.startScan(filters, settings, scanCallback);
    sendStateLocalBroadcast(ACTION_SCAN_START);
  }

  private void restartService() {
    ScanParameters scanParameters = new ScanParameters.Builder()
        .setScanInterval(scanInterval)
        .setScanPeriod(scanPeriod)
        .setFilterUUIDData(filterData)
        .build();
    ScanAlarmManager.startScanAlarm(getApplicationContext(), scanParameters);
  }

  private boolean isBLeEnabled() {
    return adapter != null && adapter.isEnabled();
  }

  private void sendStateLocalBroadcast(String action) {
    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(
        new Intent(action));
  }
}
