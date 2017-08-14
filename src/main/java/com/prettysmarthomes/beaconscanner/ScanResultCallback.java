package com.prettysmarthomes.beaconscanner;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.util.SparseArray;

import java.util.List;

import javax.inject.Inject;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static com.prettysmarthomes.beaconscanner.BLeScanService.TAG;

public class ScanResultCallback extends ScanCallback {

  private Context context;

  @Inject
  ScanResultCallback(Context context) {
    this.context = context;
  }

  @Override
  public void onBatchScanResults(List<ScanResult> results) {
    super.onBatchScanResults(results);
    for (ScanResult result : results) {
      ScanRecord scanRecord = result.getScanRecord();
      if (scanRecord != null) {
        SparseArray<byte[]> beaconData = scanRecord.getManufacturerSpecificData();
        int size = beaconData.size();
        for (int i = 0; i < size; i++) {
          int key = beaconData.keyAt(i);
          byte[] beaconContent = beaconData.get(key);
          Log.d(TAG, "sending = [" + BLeScanServiceUtils.bytesToHex(beaconContent) + "]");
          Intent beaconIntent = new Intent(BLeScanService.ACTION_BEACON_FOUNDED);
          beaconIntent.putExtra(BLeScanService.EXTRA_BEACON_CONTENT, beaconContent);
          context.sendBroadcast(beaconIntent);
        }
      }
    }
  }
}
