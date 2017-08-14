package com.prettysmarthomes.beaconscanner;

import android.support.annotation.Nullable;

public class BLeScanServiceUtils {

  final private static char[] hexArray = "0123456789ABCDEF".toCharArray();

  @Nullable
  public static String bytesToHex(@Nullable byte[] bytes) {
    if (bytes == null) {
      return null;
    }
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }
}
