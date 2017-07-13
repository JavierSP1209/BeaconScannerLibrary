package com.prettysmarthomes.beaconscannerlib;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

public class ScanAlarmManager {
  public void startScanAlarm(@NonNull Context context, @NonNull ScanParameters scanParameters) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    queryIntent.putExtra(BLeScanService.EXTRA_SCAN_PARAMS, scanParameters);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);

    // schedule the intent for future delivery
    alarmManager.set(AlarmManager.RTC,
        System.currentTimeMillis() + scanParameters.getScanInterval(), pendingQueryIntent);
  }

  public void cancelScanAlarm(@NonNull Context context) {
    AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    Intent queryIntent = new Intent(context, BLeStartScanBroadcastReceiver.class);
    PendingIntent pendingQueryIntent = PendingIntent.getBroadcast(context, 0, queryIntent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    alarmManager.cancel(pendingQueryIntent);
  }
}
