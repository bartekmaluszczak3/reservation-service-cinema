package com.example.reservation.service.cinema.service.security;

import lombok.RequiredArgsConstructor;
import org.example.authservice.client.AuthServiceClient;
import org.example.authservice.client.config.AuthClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;

@Configuration
public class SecurityConfiguration {

    @Value("${service.authservice.client.url:default}")
    private String url;

    @Value("${service.authservice.client.timeout:1ms}")
    private Duration timeout;

    @Bean
    public UserDetailsService userDetailsService() {
        return authServiceClient()::sendGetInfo;
    }

    @Bean
    public AuthClientConfiguration authClientConfiguration(){
        return new AuthClientConfiguration(url, timeout);
    }

    @Bean
    public AuthServiceClient authServiceClient(){
        return new AuthServiceClient(authClientConfiguration());
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
