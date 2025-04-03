package com.example.reservation.service.cinema.service.web;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7777"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"service.crypto.disabled=true"})
public class SeanceControllerTest {
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
    void shouldReturnReservedSeat(){
        // given
        String seanceUid = "seance-id1";

        // when
        List<String> reservedSeat = sendGetReservedSeatRequest(seanceUid);

        // then
        Assertions.assertTrue(reservedSeat.contains("1"));
        Assertions.assertTrue(reservedSeat.contains("12"));
    }

    @Test
    void shouldReturnEmptySeat() {
        String seanceUid = "seance-id9";

        // when
        List<String> reservedSeat = sendGetReservedSeatRequest(seanceUid);

        // then
        Assertions.assertTrue(reservedSeat.isEmpty());
    }

    @Test
    void shouldThrowNotFoundException(){
        String seanceUid = "not-found-id";

        // when and then
        Assertions.assertThrows(Exception.class, ()-> sendGetReservedSeatRequest(seanceUid));
    }


    @SneakyThrows
    private List<String> sendGetReservedSeatRequest(String seanceUid) {
        String url = "/api/v1/seance/reserved/" + seanceUid;
        ResponseEntity<String> entity = testRestTemplate.getForEntity(url, String.class);
        return Arrays.asList(new ObjectMapper().readValue(entity.getBody(), String[].class));
    }
}
