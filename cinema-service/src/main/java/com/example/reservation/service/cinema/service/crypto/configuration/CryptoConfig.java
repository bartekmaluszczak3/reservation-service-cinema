package com.example.reservation.service.cinema.service.crypto.configuration;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@AllArgsConstructor
@RequiredArgsConstructor
@Getter
@ConfigurationProperties("service.crypto")
@Configuration
@Setter
public class CryptoConfig {

    private boolean disabled = true;
    private String secretKey;
}
