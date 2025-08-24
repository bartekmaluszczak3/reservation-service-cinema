package com.example.reservation.service.cinema.service.web;

import com.example.reservation.service.cinema.domain.dto.ReservationDto;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.AuthServiceClientMock;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import org.example.authservice.client.config.AuthClientConfiguration;
import org.example.authservice.domain.entity.User;
import org.example.authservice.filter.utils.JwtGenerator;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7777"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "service.crypto.disabled=true",
        "service.jwt.enabled=true",
        "service.authservice.client.url=http://localhost:8080",
        "service.authservice.client.timeout=2s"})
public class ReservationControllerTest {
    @Autowired
    TestRestTemplate testRestTemplate;

    @Autowired
    private AuthClientConfiguration authClientConfiguration;

    private AuthServiceClientMock authServiceClientMock;
    private JwtGenerator jwtGenerator;

    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        jwtGenerator = new JwtGenerator(100000000, 10000, "2ADDFD5C436226A765CHSADFDAS33212332138A3BE26A");
        authServiceClientMock = new AuthServiceClientMock(authClientConfiguration);
        authServiceClientMock.start();
        container.initDatabase();
        container.initRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
        authServiceClientMock.stop();
    }

    @AfterEach
    void afterEach(){
        authServiceClientMock.resetStubs();
    }

    @Test
    void shouldFindUserReservations(){
        // given
        authServiceClientMock.stubResponse("email", "user-id");
        User user = User.builder()
                .email("email")
                .userUid("user-id")
                .password("password")
                .build();
        String jwt = jwtGenerator.generateToken(user);

        // when
        var response  = sendRequest(jwt);

        // then
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        var reservations = response.getBody();
        Assertions.assertEquals(2, reservations.size());
    }

    @Test
    void shouldReturnEmptyListIfUserDoesNotHaveReservations(){
        authServiceClientMock.stubResponse("email", "user-uid-123");
        User user = User.builder()
                .email("email")
                .userUid("user-uid-123")
                .password("password")
                .build();
        String jwt = jwtGenerator.generateToken(user);

        // when
        var response  = sendRequest(jwt);

        // then
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        var reservations = response.getBody();
        Assertions.assertEquals(0, reservations.size());
    }


    private ResponseEntity<List<ReservationDto>> sendRequest(String jwt){
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer "+ jwt);
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "/api/v1/reservation";
        return testRestTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ReservationDto>>() {});
    }
}
