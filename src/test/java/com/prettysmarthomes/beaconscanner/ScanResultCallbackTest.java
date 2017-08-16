package com.prettysmarthomes.beaconscanner;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.util.SparseArray;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.LinkedList;
import java.util.List;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ScanResultCallbackTest {
  private ScanResultCallback subject;
  @Mock
  private Context context;

  @Before
  public void setUp() throws Exception {
    initMocks(this);
    subject = new ScanResultCallback(context);
  }

  @Test
  public void onBatchScanResults_whenNoManufacturerIdSet_shouldSendBroadcastWithBeaconInformation() {
    List<ScanResult> resultList = new LinkedList<>();
    ScanRecord scanRecord = mock(ScanRecord.class);
    byte[] beaconContent = {1, 2, 3, 4, 5, 6};
    byte[] beaconContentA = {2, 3, 4, 5, 6, 1};
    byte[] beaconContentB = {3, 4, 5, 6, 1, 2};

    SparseArray<byte[]> multipleContent = new SparseArray<>();
    multipleContent.put(1, beaconContent);
    multipleContent.put(2, beaconContentA);
    multipleContent.put(3, beaconContentB);
    when(scanRecord.getManufacturerSpecificData()).thenReturn(multipleContent);
    resultList.add(new ScanResult(mock(BluetoothDevice.class), scanRecord, 0, 10000));

    ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
    subject.onBatchScanResults(resultList);
    verify(context, times(multipleContent.size())).sendBroadcast(intentArgumentCaptor.capture());

    List<Intent> actualIntents = intentArgumentCaptor.getAllValues();
    assertBeaconFoundIntent(actualIntents.get(0), beaconContent);
    assertBeaconFoundIntent(actualIntents.get(1), beaconContentA);
    assertBeaconFoundIntent(actualIntents.get(2), beaconContentB);
  }

  @Test
  public void onBatchScanResults_whenResultHasScanRecord_shouldSendBroadcastWithBeaconInformation() {
    int myFilter = 142;
    List<ScanResult> resultList = new LinkedList<>();
    ScanRecord scanRecord = mock(ScanRecord.class);
    byte[] expectedContent = {1, 2, 3, 4, 5, 6};
    SparseArray<byte[]> multipleContent = new SparseArray<>();
    multipleContent.put(myFilter, expectedContent);
    when(scanRecord.getManufacturerSpecificData()).thenReturn(multipleContent);
    resultList.add(new ScanResult(mock(BluetoothDevice.class), scanRecord, 0, 10000));

    ArgumentCaptor<Intent> intentArgumentCaptor = ArgumentCaptor.forClass(Intent.class);
    subject.onBatchScanResults(resultList);
    verify(context).sendBroadcast(intentArgumentCaptor.capture());

    Intent actualIntent = intentArgumentCaptor.getValue();
    assertBeaconFoundIntent(actualIntent, expectedContent);
  }

  private void assertBeaconFoundIntent(Intent actualIntent, byte[] expectedContent) {
    assertThat(actualIntent.getAction(), is(equalTo(BLeScanService.ACTION_BEACON_FOUNDED)));
    assertThat(actualIntent.getByteArrayExtra(BLeScanService.EXTRA_BEACON_CONTENT),
        is(expectedContent));
  }
}