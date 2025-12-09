package com.orangeplannet.zjuhelper.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

public class RsaEncryptionUtil {

    public static String encrypt(String password, String modulusHex, String exponentHex) {
        try {
            // 1. 将十六进制的模数 (Modulus) 和指数 (Exponent) 转换为 BigInteger
            BigInteger modulus = new BigInteger(modulusHex, 16);
            BigInteger exponent = new BigInteger(exponentHex, 16);

            // 2. 将密码转换为 UTF-8 字节数组
            byte[] passwordBytes = password.getBytes(StandardCharsets.UTF_8);
            
            // 3. 将字节数组转换为 BigInteger
            // 参数 1 表示这是一个正数。Java 的 BigInteger 默认是大端序 (Big-Endian)，
            // 这与大多数网络传输标准一致，直接转换即可。
            BigInteger payload = new BigInteger(1, passwordBytes); 

            // 4. 验证数据长度：明文转换后的数值不能大于模数，否则无法正确加密
            if (payload.compareTo(modulus) >= 0) {
                throw new IllegalArgumentException("Password is too long for RSA encryption");
            }

            // 5. 执行 RSA 加密核心运算： ciphertext = payload^exponent mod modulus
            BigInteger encrypted = payload.modPow(exponent, modulus);

            // 6. 将加密结果转换回十六进制字符串
            String hex = encrypted.toString(16);
            
            // 7. 格式化处理：确保生成的十六进制字符串长度为偶数
            // 如果长度是奇数，说明转换时省略了前导零，需要补齐
            if (hex.length() % 2 != 0) {
                hex = "0" + hex;
            }
            
            return hex;

        } catch (Exception e) {
            throw new RuntimeException("RSA Encryption failed", e);
        }
    }
}
