package com.example.reservation.service.cinema.service.web;

import com.example.reservation.service.cinema.domain.dto.ReservationDto;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "service.crypto.disabled=true",
        "service.jwt.enabled=false"})
public class ReservationControllerWithoutJwtTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
        container.initRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
    }

    @Test
    void shouldReturnEmptyListWhenJwtIsDisabled(){
        // when
        var response  = sendRequest();

        // then
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        var reservations = response.getBody();
        Assertions.assertEquals(0, reservations.size());
    }

    private ResponseEntity<List<ReservationDto>> sendRequest(){
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<String> entity = new HttpEntity<>(headers);
        String url = "/api/v1/reservation";
        return testRestTemplate.exchange(url, HttpMethod.GET, entity, new ParameterizedTypeReference<List<ReservationDto>>() {});
    }

}
