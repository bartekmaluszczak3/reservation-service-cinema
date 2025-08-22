package com.example.reservation.service.cinema.service.filter;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;

import java.io.IOException;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7777"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {
        "service.authservice.client.url=http://localhost:8080",
        "service.authservice.client.timeout=2s",
        "service.jwt.enabled=false"
})
public class JwtFilterDisabledTest {
    private static final PostgresContainer container = new PostgresContainer();

    @Autowired
    TestRestTemplate testRestTemplate;

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
    void shouldReturnOkWhenUserDoesNotHaveJwt(){
        // when
        ResponseEntity<String> result = sendRequest();

        // then
        Assertions.assertTrue(result.getStatusCode().is2xxSuccessful());
    }

    private ResponseEntity<String> sendRequest(){
        String url = "/api/v1/seance/reserved/seance-id1";
        return testRestTemplate.exchange(url, HttpMethod.GET, null, String.class);
    }

}
