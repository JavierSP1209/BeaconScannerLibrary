package com.prettysmarthomes.beaconscannerlib;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.prettysmarthomes.beaconscannerlib.ScanParameters.ManufacturerID;

import java.util.List;

import javax.inject.Inject;

import no.nordicsemi.android.support.v18.scanner.ScanCallback;
import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

class ScanResultCallback extends ScanCallback {

  private Context context;

  @Inject
  ScanResultCallback(Context context) {
    this.context = context;
  }

  @Override
  public void onBatchScanResults(List<ScanResult> results) {
    super.onBatchScanResults(results);
    Log.d(BLeScanService.TAG, "onBatchScanResults() called");
    for (ScanResult result : results) {
      Log.d(BLeScanService.TAG, "result = [" + result + "]");
      ScanRecord scanRecord = result.getScanRecord();
      if (scanRecord != null) {
        byte[] beaconContent = scanRecord.getManufacturerSpecificData(ManufacturerID.I_BEACON);
        Intent beaconIntent = new Intent(BLeScanService.ACTION_BEACON_FOUND);
        beaconIntent.putExtra(BLeScanService.EXTRA_BEACON_CONTENT, beaconContent);
        context.sendBroadcast(beaconIntent);
      }
    }
  }
}
