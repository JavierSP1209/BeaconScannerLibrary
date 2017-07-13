package com.prettysmarthomes.beaconscannerlib;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;


public class BLeStartScanBroadcastReceiver extends BroadcastReceiver {
  @Override
  public void onReceive(Context context, Intent intent) {
    Intent queryIntent = new Intent(context, BLeScanService.class);
    queryIntent.putExtras(intent.getExtras());
    context.startService(queryIntent);
  }
}
