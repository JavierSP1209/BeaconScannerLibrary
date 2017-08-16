package com.prettysmarthomes.beaconscanner;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class BLeStartScanBroadcastReceiver extends BroadcastReceiver {

  public static final String EXTRA_SCAN_PERIOD = "EXTRA_SCAN_PERIOD";
  public static final String EXTRA_SCAN_INTERVAL = "EXTRA_SCAN_INTERVAL";
  public static final String EXTRA_MANUFACTURER_ID = "EXTRA_MANUFACTURER_ID";
  public static final String EXTRA_FILTER = "EXTRA_FILTER";

  @Override
  public void onReceive(Context context, Intent intent) {
    Intent queryIntent = new Intent(context, BLeScanService.class);
    Bundle extras = intent.getExtras();
    ScanParameters scanParameters = new ScanParameters.Builder()
        .setFilterUUIDData(extras.getByteArray(EXTRA_FILTER))
        .setScanInterval(extras.getLong(EXTRA_SCAN_INTERVAL))
        .setScanPeriod(extras.getLong(EXTRA_SCAN_PERIOD))
        .setManufacturerId(extras.getInt(EXTRA_MANUFACTURER_ID))
        .build();
    queryIntent.putExtra(BLeScanService.EXTRA_SCAN_PARAMS, scanParameters);
    context.startService(queryIntent);
  }
}
