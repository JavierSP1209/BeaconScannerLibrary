package com.prettysmarthomes.beaconscanner;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import javax.inject.Inject;

import static com.prettysmarthomes.beaconscanner.BLeStartScanBroadcastReceiver.EXTRA_FILTER;
import static com.prettysmarthomes.beaconscanner.BLeStartScanBroadcastReceiver.EXTRA_MANUFACTURER_ID;
import static com.prettysmarthomes.beaconscanner.BLeStartScanBroadcastReceiver.EXTRA_SCAN_INTERVAL;
import static com.prettysmarthomes.beaconscanner.BLeStartScanBroadcastReceiver.EXTRA_SCAN_PERIOD;

public class BLeScanServiceManager {

  @Inject
  public BLeScanServiceManager() {
  }

  public void startScanService(@NonNull Context context, @NonNull ScanParameters scanParameters) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);

    queryIntent.putExtra(EXTRA_SCAN_PERIOD, scanParameters.getScanPeriod());
    queryIntent.putExtra(EXTRA_SCAN_INTERVAL, scanParameters.getScanInterval());
    queryIntent.putExtra(EXTRA_FILTER, scanParameters.getFilterUUIDData());
    queryIntent.putExtra(EXTRA_MANUFACTURER_ID, scanParameters.getManufacturerId());
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    // schedule the intent for future delivery
    alarmManager.set(AlarmManager.RTC,
        System.currentTimeMillis() + scanParameters.getScanInterval(), pendingQueryIntent);
  }

  public void cancelScanService(@NonNull Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingQueryIntent);
  }
}
