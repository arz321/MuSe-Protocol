package com.example.nirjon.bledemo4_advertising;

//import android.bluetooth.le.AdvertiseData;
//import android.text.TextUtils;
import android.util.Log;
//import android.widget.Toast;
import com.example.nirjon.bledemo4_advertising.util.BLEUtil;



/* loaded from: classes.dex */
public class BleUtil {
    public static final String HEX = "0123456789abcdef";
    //public String rawAddress = "77 62 4d 53 45";

    /*
        public static AdvertiseData createScanAdvertiseData(int i, byte[] bArr) {
            AdvertiseData.Builder builder = new AdvertiseData.Builder();
            builder.addManufacturerData(i, bArr);
            return builder.build();
        }

        public static AdvertiseData createIBeaconAdvertiseData(int i, byte[] bArr) {
            AdvertiseData.Builder builder = new AdvertiseData.Builder();
            builder.addManufacturerData(i, bArr);
            return builder.build();
        }
    */
    public static byte[] getBleCommand(String str, String str2) {
        String lowerCase = str.replace(" ", "").toLowerCase();
        int i = 0;
        if (lowerCase.length() < 6 || lowerCase.length() > 10) {
            //Toast.makeText(MuSeApplication.mContext, "Please input address with length between 3 and 5 bytes", 0).show();
            return null;
        }
        int length = lowerCase.length() / 2;
        byte[] bArr = new byte[length];
        int i2 = 0;
        while (i2 < length) {
            int i3 = i2 + 1;
            bArr[i2] = strToByte(lowerCase.substring(i2 * 2, i3 * 2));
            i2 = i3;
        }
        String lowerCase2 = str2.replace(" ", "").toLowerCase();
        if (lowerCase2.length() < 2) {
            //Toast.makeText(MuSeApplication.mContext, "The payload is at least 1 byte", 0).show();
            return null;
        }
        int length2 = lowerCase2.length() / 2;
        byte[] bArr2 = new byte[length2];
        while (i < length2) {
            int i4 = i + 1;
            bArr2[i] = strToByte(lowerCase2.substring(i * 2, i4 * 2));
            i = i4;
        }
        int i5 = length + length2 + 5;
        byte[] bArr3 = new byte[i5];
        BLEUtil.get_rf_payload(bArr, length, bArr2, length2, bArr3);
        Log.i("联网 蓝牙指令：" + str2, bytesToStr(bArr3, i5));
        return bArr3;
    }

    /*    public static byte[] getBleCommand(String str) {
            if (!TextUtils.isEmpty(MuSeApplication.broadcastPrefix)) {
                return getBleCommand(MuSeApplication.broadcastPrefix, str);
            }
            return getBleCommand(rawAddress, str);
        }
    */
    public static byte strToByte(String str) {
        int charToByte;
        if (str.length() == 1) {
            charToByte = HEX.indexOf(str) & 255;
        } else {
            charToByte = charToByte(str.charAt(1)) | (charToByte(str.charAt(0)) << 4);
        }
        return (byte) charToByte;
    }

    public static byte charToByte(char c) {
        return (byte) HEX.indexOf(c);
    }

    public static String bytesToStr(byte[] bArr, int i) {
        StringBuilder sb = new StringBuilder("");
        for (int i2 = 0; i2 != i; i2++) {
            if ((bArr[i2] & 240) == 0) {
                sb.append("0");
            } else {
                sb.append(HEX.charAt((bArr[i2] >> 4) & 15));
            }
            sb.append(HEX.charAt(bArr[i2] & 15));
            sb.append(" ");
        }
        return sb.toString();
    }
}
