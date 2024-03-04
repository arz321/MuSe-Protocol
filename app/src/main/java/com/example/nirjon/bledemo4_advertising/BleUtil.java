package com.example.nirjon.bledemo4_advertising;

//import android.bluetooth.le.AdvertiseData;
//import android.text.TextUtils;
import android.util.Log;
//import android.widget.Toast;
//import com.example.nirjon.bledemo4_advertising.util.BLEUtil;



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
        //BLEUtil.get_rf_payload(bArr, length, bArr2, length2, bArr3);
        get_rf_payload(bArr, length, bArr2, length2, bArr3);
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

    public static void get_rf_payload(byte[] bArr, int length, byte[] bArr2, int length2, byte[] bArr3) {

        byte[] ctx_25 = new byte[7];
        byte[] ctx_3F = new byte[7];

        whitening_init(0x25, ctx_25); //1100101
        whitening_init(0x3f, ctx_3F); //1111111

        int length_24 = 0x12 + length + length2;
        int length_26 = length_24 + 0x02;

        byte[] result_25 = new byte[length_26];
        byte[] result_3f = new byte[length_26];
        byte[] resultbuf = new byte[length_26];

        resultbuf[0x0f] = 0x71;//const buf[0x0f-0x11]
        resultbuf[0x10] = 0x0f;
        resultbuf[0x11] = 0x55;

        if (length > 0) {
            for (byte j = 0; j < length; j++) { //flip bArr[] and write to buf[0x12-0x16]
                resultbuf[0x12 + length - j - 0x01] = bArr[j];
            }
        }

        if (length2 > 0) {
            for (byte j = 0; j < length2; j++) { //flip bArr2[] and write to buf[0x17]
                resultbuf[length_24 - j - 0x01] = bArr2[j];
            }
        }

        for (byte i = 0; i < 0x03 + length; i++) { //invert_8 byte buf[0x0f-0x16]
            resultbuf[0x0f + i] = invert_8(resultbuf[0x0f + i]);
        }

        int crc16 = check_crc16(bArr, length, bArr2, length2); //write crc16 to buf[0x18-0x19]
        resultbuf[length_24] = (byte) crc16;
        resultbuf[length_24 + 1] = (byte) (crc16 >> 8);

        whitenging_encode(resultbuf, 0x2 + length + length2, ctx_3F, 0x12, result_3f);
        whitenging_encode(resultbuf, length_26, ctx_25, 0x00, result_25);

        for (byte i = 0; i < length_26; i++) { //XOR result_25[] and result_3f[]
            result_25[i] ^= result_3f[i];
        }

        System.arraycopy(result_25, 0x0f, bArr3, 0, 0x0b); //copy result_25[0x0f-0x19] to bArr3
    }

    public static void whitening_init(int val, byte[] ctx) {
        ctx[0] = 1;
        ctx[1] = (byte) ((val >> 5) & 1);
        ctx[2] = (byte) ((val >> 4) & 1);
        ctx[3] = (byte) ((val >> 3) & 1);
        ctx[4] = (byte) ((val >> 2) & 1);
        ctx[5] = (byte) ((val >> 1) & 1);
        ctx[6] = (byte) (val & 1);
    }

    public static int check_crc16(byte[] addr, int addrLength, byte[] data, int dataLength) {
        int crc = 0xffff;

        for (int i = addrLength - 1; i >= 0; i--) {
            crc ^= addr[i] << 8;
            for (int ii = 0; ii < 8; ii++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }

        for (int i = 0; i < dataLength; i++) {
            crc ^= invert_8(data[i]) << 8;
            for (int ii = 0; ii < 8; ii++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ 0x1021;
                } else {
                    crc <<= 1;
                }
            }
        }
        crc = ~invert_16(crc) & 0xffff;
        return crc;
    }

    public static byte invert_8(byte value) {
        byte result = 0;
        for (byte i = 0; i < 8; i++) {
            result <<= 1;
            result |= (value & 1);
            value >>= 1;
        }
        return result;
    }

    public static int invert_16(int value) {
        int result = 0;
        for (int i = 0; i < 16; i++) {
            result <<= 1;
            result |= (value & 1);
            value >>= 1;
        }
        return result;
    }

    public static void whitenging_encode(byte[] data, int len, byte[] ctx, int offset, byte[] result) {
        System.arraycopy(data, 0, result, 0, len);
        for (int i = 0; i < len; i++) {
            int var6 = ctx[6];
            int var5 = ctx[5];
            int var4 = ctx[4];
            int var3 = ctx[3];
            int var52 = var5 ^ ctx[2];
            int var41 = var4 ^ ctx[1];
            int var63 = var6 ^ ctx[3];
            int var630 = var63 ^ ctx[0];

            ctx[0] = (byte)(var52 ^ var6);
            ctx[1] = (byte)var630;
            ctx[2] = (byte)var41;
            ctx[3] = (byte)var52;
            ctx[4] = (byte)(var52 ^ var3);
            ctx[5] = (byte)(var630 ^ var4);
            ctx[6] = (byte)(var41 ^ var5);

            int c = result[i + offset];
            result[i + offset] = (byte)(((c & 0x80) ^ ((var52 ^ var6) << 7)) +
                                        ((c & 0x40) ^ (var630 << 6)) +
                                        ((c & 0x20) ^ (var41 << 5)) +
                                        ((c & 0x10) ^ (var52 << 4)) +
                                        ((c & 0x08) ^ (var63 << 3)) +
                                        ((c & 0x04) ^ (var4 << 2)) +
                                        ((c & 0x02) ^ (var5 << 1)) +
                                        ((c & 0x01) ^ (var6)));
        }
    }
}
