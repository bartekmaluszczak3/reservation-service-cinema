package com.example.reservation.service.cinema.service.utils;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.example.authservice.client.config.AuthClientConfiguration;
import org.example.authservice.domain.entity.User;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static com.github.tomakehurst.wiremock.client.WireMock.*;

@RequiredArgsConstructor
public class AuthServiceClientMock {
    private WireMockServer server;
    private String baseUrl;

    public AuthServiceClientMock(AuthClientConfiguration authClientConfiguration){
        this.baseUrl = authClientConfiguration.getUrl();
        this.server = new WireMockServer(new WireMockConfiguration().port(8080)
                .timeout(authClientConfiguration.getTimeout().toMillisPart()));
    }

    @SneakyThrows
    public void stubResponse(String email){
        User user = User.builder()
                .userUid("userUid")
                .password("password")
                .email(email)
                .id(1)
                .build();
        String serializedUser = new ObjectMapper().writeValueAsString(user);
        server.stubFor(get(urlEqualTo("/api/v1/auth/getInfo?email=" + email))
                .willReturn(aResponse().withBody(serializedUser)));

    }
    public void start(){
        server.start();
    }

    public void stop(){
        server.stop();
    }

    public void resetStubs(){
        server.resetAll();
    }
}
