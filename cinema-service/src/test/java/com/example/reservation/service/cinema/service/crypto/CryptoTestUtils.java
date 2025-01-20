package com.example.reservation.service.cinema.service.crypto;

import lombok.SneakyThrows;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

public class CryptoTestUtils {

    @SneakyThrows
    public static String decrypt(SecretKeySpec secretKey, String strToDecrypt){;
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return new String(cipher.doFinal(Base64.getDecoder().decode(strToDecrypt)));
    }

    @SneakyThrows
    public static SecretKeySpec createSecretSpec(String myKey){
        byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
        var sha = MessageDigest.getInstance("SHA-1");
        key = sha.digest(key);
        key = Arrays.copyOf(key, 16);
        return new SecretKeySpec(key, "AES");
    }
}
