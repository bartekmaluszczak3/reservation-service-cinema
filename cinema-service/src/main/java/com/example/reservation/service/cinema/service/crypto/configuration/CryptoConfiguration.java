package com.example.reservation.service.cinema.service.crypto.configuration;

import com.example.reservation.service.cinema.service.crypto.encrypter.Encrypter;
import com.example.reservation.service.cinema.service.crypto.encrypter.NoSecureEncrypter;
import com.example.reservation.service.cinema.service.crypto.encrypter.AESEncrypter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class CryptoConfiguration {

    private final CryptoConfig cryptoConfig;

    @Bean
    public Encrypter encrypter() throws Exception {
        if (cryptoConfig.isDisabled()){
            return new NoSecureEncrypter();
        }else {
            return new AESEncrypter(cryptoConfig.getSecretKey());
        }
    }
}
