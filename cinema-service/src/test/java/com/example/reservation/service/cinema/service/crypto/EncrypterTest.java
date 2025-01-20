package com.example.reservation.service.cinema.service.crypto;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.crypto.encrypter.Encrypter;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import javax.crypto.spec.SecretKeySpec;


@TestPropertySource(properties = {"service.crypto.disabled=false",
    "service.crypto.secretKey=secret"
})
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
public class EncrypterTest {

    @Autowired
    Encrypter encrypter;

    @Test
    void shouldLoadProperEncrypter(){
        Assertions.assertTrue(encrypter.getClass().getName().contains("AES"));
    }

    @SneakyThrows
    @Test
    void shouldEncryptData(){
        // given
        String dataToEncrypt = "testData";

        // when
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("secret");
        var encrypted = encrypter.encrypt(dataToEncrypt);

        // then
        var decrypted= CryptoTestUtils.decrypt(spec, encrypted);
        Assertions.assertEquals(dataToEncrypt, decrypted);
    }

    @SneakyThrows
    @Test
    void shouldEncryptLargeAmountOfData(){
        // given
        var largeString = createDataSize(20000000);

        // when
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("secret");
        var encrypted = encrypter.encrypt(largeString);

        // then
        var decrypted = CryptoTestUtils.decrypt(spec, encrypted);
        Assertions.assertEquals(largeString, decrypted);
    }


    private String createDataSize(int msgSize) {
        return "a".repeat(Math.max(0, msgSize));
    }
}
