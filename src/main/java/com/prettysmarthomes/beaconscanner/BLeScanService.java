package com.prettysmarthomes.beaconscanner;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.prettysmarthomes.beaconscanner.di.BleScanServiceBaseModule;
import com.prettysmarthomes.beaconscanner.di.DaggerBLeScanServiceComponent;

import java.util.ArrayList;
import java.util.Arrays;
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
  public static final String ACTION_BEACON_FOUNDED = "com.prettysmarthomes.beaconscanner.BEACON_FOUNDED";
  public static final String ACTION_SCAN_STARTED = "com.prettysmarthomes.beaconscanner.SCAN_STARTED";
  public static final String ACTION_SCAN_STOPPED = "com.prettysmarthomes.beaconscanner.SCAN_STOPPED";

  public static final String EXTRA_BEACON_CONTENT = "com.prettysmarthomes.beaconscanner.BEACON_CONTENT";
  public static final String EXTRA_SCAN_PARAMS = "com.prettysmarthomes.beaconscanner.SCAN_PARAMS";
  public static final String TAG = "BleScanService";

  private Handler stopScanHandler;
  @Inject
  BluetoothAdapter adapter;
  @Inject
  ScanResultCallback scanCallback;
  @Inject
  BluetoothLeScannerCompat scanner;
  @Inject
  BLeScanServiceManager bleScanServiceManager;

  private Runnable serviceStarter = new Runnable() {
    @Override
    public void run() {
      scanner.stopScan(scanCallback);
      sendStateLocalBroadcast(ACTION_SCAN_STOPPED);
    }
  };

  public BLeScanService() {
    super(TAG);
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
    ScanParameters scanParams = intent.getParcelableExtra(EXTRA_SCAN_PARAMS);
    if (scanParams != null) {
      if (isBLeEnabled()) {
        startScan(scanParams);
        bleScanServiceManager.startScanService(getApplicationContext(), scanParams);
        stopScanHandler.postDelayed(serviceStarter, scanParams.getScanPeriod());
      } else {
        Log.e(TAG, "onHandleIntent: Ble disabled, service not started");
      }
    } else {
      throw new IllegalStateException("Extra 'com.prettysmarthomes.beaconscanner.SCAN_PARAMS' not set");
    }
  }

  private void startScan(@NonNull ScanParameters scanParams) {
    ScanSettings settings = new ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_BALANCED).setReportDelay(
            ScanParameters.SCAN_RESULTS_DELAY)
        .setUseHardwareBatchingIfSupported(false).build();
    List<ScanFilter> filters = new ArrayList<>();
    ScanFilter.Builder builder = new ScanFilter.Builder();
    int manufacturerId = scanParams.getManufacturerId();
    //Negative manufacturer id is consider invalid by the filter, so if its not valid do not set it
    if (manufacturerId > 0) {
      byte[] mask = MASK;
      byte[] filterUUIDData = scanParams.getFilterUUIDData();
      //If filter data is null, do not include mask
      if (filterUUIDData == null) {
        mask = null;
        Log.e(TAG, "null FilterData, mask ignored: ");
      } else {
        if (filterUUIDData.length != mask.length) {
          Log.e(TAG, "invalid FilterData, for mask: " + Arrays.toString(mask));
          filterUUIDData = null;
          mask = null;
        }
      }

      builder.setManufacturerData(manufacturerId, filterUUIDData, mask);
    } else {
      Log.e(TAG, "Invalid(negative) manufacturerID set: " + manufacturerId);
    }
    filters.add(builder.build());
    scanner.startScan(filters, settings, scanCallback);
    sendStateLocalBroadcast(ACTION_SCAN_STARTED);
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
