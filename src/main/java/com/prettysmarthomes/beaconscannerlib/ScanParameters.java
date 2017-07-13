package com.prettysmarthomes.beaconscannerlib;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Class containing the scan parameters to use, this class uses the builder pattern to set only the
 * desired parameters Created by javier.silva on 5/22/16.
 */
public class ScanParameters implements Parcelable {
  /**
   * Default scan interval for the scan service in milliseconds, a period is the time between
   * scans
   */
  static final long DEFAULT_BLE_SCAN_INTERVAL_MS = 10000;
  /**
   * Default scan period, the actual scanning time for each scan
   */
  static final long DEFAULT_BLE_SCAN_PERIOD_MS = 5000;
  static final long SCAN_RESULTS_DELAY = 1000;

  public static class ManufacturerID {
    public static final int I_BEACON = 0x4C;
    public static final int NORDIC = 0x59;
  }

  private long scanPeriod;
  private long scanInterval;
  private int manufacturerId;
  private byte[] filterUUIDData;

  private ScanParameters(long scanPeriod, long scanInterval, int manufacturerId, byte[] filterUUIDData) {
    this.scanPeriod = scanPeriod;
    this.scanInterval = scanInterval;
    this.filterUUIDData = filterUUIDData;
    this.manufacturerId = manufacturerId;
  }

  private ScanParameters(Parcel in) {
    scanPeriod = in.readLong();
    scanInterval = in.readLong();
    manufacturerId = in.readInt();
    filterUUIDData = in.createByteArray();
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(scanPeriod);
    dest.writeLong(scanInterval);
    dest.writeInt(manufacturerId);
    dest.writeByteArray(filterUUIDData);
  }

  @Override
  public int describeContents() {
    return 0;
  }

  public static final Creator<ScanParameters> CREATOR = new Creator<ScanParameters>() {
    @Override
    public ScanParameters createFromParcel(Parcel in) {
      return new ScanParameters(in);
    }

    @Override
    public ScanParameters[] newArray(int size) {
      return new ScanParameters[size];
    }
  };

  public long getScanPeriod() {
    return scanPeriod;
  }

  public long getScanInterval() {
    return scanInterval;
  }

  public byte[] getFilterUUIDData() {
    return filterUUIDData;
  }

  public int getManufacturerId() {
    return manufacturerId;
  }

  @Override
  public String toString() {
    return "ScanParameters{" +
        "scanPeriod=" + scanPeriod +
        ", scanInterval=" + scanInterval +
        ", manufacturerId=" + manufacturerId +
        ", filterUUIDData=" + Arrays.toString(filterUUIDData) +
        '}';
  }

  public static class Builder {
    private long nestedScanPeriod = DEFAULT_BLE_SCAN_PERIOD_MS;
    private long nestedScanInterval = DEFAULT_BLE_SCAN_INTERVAL_MS;
    private int nestedManufacturerId = -1;
    private byte[] nestedFilterUUIDData;

    public Builder setScanPeriod(long scanPeriod) {
      this.nestedScanPeriod = scanPeriod;
      return this;
    }

    public Builder setScanInterval(long scanInterval) {
      this.nestedScanInterval = scanInterval;
      return this;
    }

    public Builder setFilterUUIDData(byte[] filterUUIDData) {
      this.nestedFilterUUIDData = filterUUIDData;
      return this;
    }

    public Builder setManufacturerId(int manufacturerId) {
      this.nestedManufacturerId = manufacturerId;
      return this;
    }

    public ScanParameters build() {
      return new ScanParameters(nestedScanPeriod, nestedScanInterval, nestedManufacturerId, nestedFilterUUIDData);
    }

  }
}
