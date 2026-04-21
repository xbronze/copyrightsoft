package cn.blockchain.copyrightsoft.utils;

import org.apache.commons.codec.digest.DigestUtils;

public class FileHashUtils {

    public static String calculateSHA256(byte[] data) {
        return DigestUtils.sha256Hex(data);
    }

    public static byte[] hexStringToBytes(String hexString) {
        int len = hexString.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4)
                    + Character.digit(hexString.charAt(i + 1), 16));
        }
        return data;
    }
}
