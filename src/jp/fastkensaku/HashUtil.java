package jp.fastkensaku;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashUtil {
    public static String getMD5(String str){
        MessageDigest md5 = null;
        try {
            md5 = MessageDigest.getInstance("MD5");
            byte[] md5_result = md5.digest(str.getBytes());
            String hashDir = String.format("%020x", new BigInteger(1, md5_result));
            return hashDir;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return "";
        }
    }
}
