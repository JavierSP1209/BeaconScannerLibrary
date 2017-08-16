package com.prettysmarthomes.beaconscanner;

import org.junit.Test;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Test to util methods
 */
public class BLeScanServiceUtilsTest {

  @Test
  public void bytesToHex() throws Exception {
    assertThat(BLeScanServiceUtils.bytesToHex(null), is(nullValue()));
    assertBytesToHex(new byte[]{}, "");
    assertBytesToHex(new byte[]{0x01, 0x02, 0x03}, "010203");
    assertBytesToHex(new byte[]{1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 0, 0, 0}, "010101010101010101010101010101010000000000");
    assertBytesToHex(new byte[]{-1, -2, 45, 18, 30, 75, 15, -92, -103, 78, -50, -75, 49, -12, 5, 69}, "FFFE2D121E4B0FA4994ECEB531F40545");
    assertBytesToHex(new byte[]{-1, -2, 45, 18, 30, 75, 15, -92, -103, 78, -50, -75, 49, -12, 5, 70}, "FFFE2D121E4B0FA4994ECEB531F40546");


  }

  private void assertBytesToHex(byte[] param, String expectedResult) {
    assertThat(BLeScanServiceUtils.bytesToHex(param), is(expectedResult));
  }

}