package com.example.nirjon.bledemo4_advertising.util;

public class BLEUtil {
    public static native void get_rf_payload(byte[] bArr, int i, byte[] bArr2, int i2, byte[] bArr3);

    static {
        System.loadLibrary("ble");
    }
}
