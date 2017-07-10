package com.prettysmarthomes.beaconscannerlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class ScanAlarmManager {
  public static void startScanAlarm(Context context, ScanParameters scanParameters) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    queryIntent.putExtra(BLeScanService.EXTRA_SCAN_PERIOD, scanParameters.getScanPeriod());
    queryIntent.putExtra(BLeScanService.EXTRA_SCAN_INTERVAL, scanParameters.getScanInterval());
    queryIntent.putExtra(BLeScanService.EXTRA_FILTER_UUID, scanParameters.getFilterUUIDData());
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    Log.d(BLeScanService.TAG, "startScanAlarm() called with: context = [" + context + "], scanParameters = [" + scanParameters + "]");
    // schedule the intent for future delivery
    alarmManager.set(AlarmManager.RTC,
        System.currentTimeMillis() + scanParameters.getScanInterval(), pendingQueryIntent);
  }

  public static void cancelScanAlarm(Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingQueryIntent);
  }
}
