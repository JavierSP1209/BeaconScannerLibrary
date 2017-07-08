package com.prettysmarthomes.beaconscannerlib;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.prettysmarthomes.beaconscannerlib.di.BleScanServiceBaseModule;
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

  private byte[] filterData;
  private long scanInterval;

  private Handler stopScanHandler;
  @Inject
  BluetoothAdapter adapter;
  @Inject
  CustomScanCallback scanCallback;
  @Inject
  BluetoothLeScannerCompat scanner;

  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      scanner.stopScan(scanCallback);
      sendStateLocalBroadcast(ACTION_SCAN_STOP);
    }
  };
  private long scanPeriod;

  public BLeScanService(String name) {
    super(name);
  }

  @Override
  public void onCreate() {
    super.onCreate();
    injectMembers();
    stopScanHandler = new Handler();
  }

  @VisibleForTesting
  protected void injectMembers() {
    DaggerBLeScanServiceComponent.builder()
        .bleScanServiceBaseModule(new BleScanServiceBaseModule(this))
        .build()
        .inject(this);
  }

  @Override
  protected void onHandleIntent(Intent intent) {
    Log.d(TAG, "onHandleIntent()");
    filterData = intent.getByteArrayExtra(EXTRA_FILTER_UUID);
    scanPeriod = intent.getLongExtra(EXTRA_SCAN_PERIOD,
        ScanParameters.DEFAULT_BLE_SCAN_PERIOD_MS);
    scanInterval = intent.getLongExtra(EXTRA_SCAN_INTERVAL,
        ScanParameters.DEFAULT_BLE_SCAN_INTERVAL_MS);
    if(filterData!=null) {
      Log.d(TAG, "filter: " + BLeScanServiceUtils.bytesToHex(filterData) + " - " + scanPeriod + " - " + scanInterval);
    }
    if (isBLeEnabled()) {
      startScan();
      restartService();
      stopScanHandler.postDelayed(serviceStarter, scanPeriod);
    }
  }

  private void startScan() {
    Log.d(TAG, "startScan() called");
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

  public void setStopScanHandler(Handler stopScanHandler) {
    this.stopScanHandler = stopScanHandler;
  }
}
