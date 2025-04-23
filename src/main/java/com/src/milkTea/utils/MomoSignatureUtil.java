package com.src.milkTea.utils;

import org.apache.commons.codec.binary.Hex;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

public class MomoSignatureUtil {

    public static String signSHA256(String rawData, String secretKey ) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256"); // Sử dụng UTF-8
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(secretKeySpec);
        byte[] result = mac.doFinal(rawData.getBytes(StandardCharsets.UTF_8)); // Sử dụng UTF-8
        return Hex.encodeHexString(result);
    }
}