package com.example.reservation.service.cinema.service.crypto;

import com.example.reservation.service.cinema.domain.dto.SeanceDto;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.util.UriComponentsBuilder;

import javax.crypto.BadPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7000"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"service.crypto.disabled=false",
                                "service.crypto.secretKey=secrets"})
public class CryptoEnabledTest {

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

    @SneakyThrows
    @Test
    void shouldDecryptSearchSeancesResponse() {
        // given
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("secrets");

        // when
        HttpEntity<String> listResponseEntity = sendSearchRequest(null, null, null);

        // then
        var decryptedResponse = CryptoTestUtils.decrypt(spec, listResponseEntity.getBody());
        var seances = responseToSeanceDtoList(decryptedResponse);
        Assertions.assertEquals(11, seances.size());
    }

    @Test
    void shouldNotDecryptSearchSeancesResponseUsingFakeSecretSpec(){
        // given
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("fake");

        // when
        HttpEntity<String> listResponseEntity = sendSearchRequest(null, null, null);

        // then
        Assertions.assertThrows(BadPaddingException.class,
                ()->CryptoTestUtils.decrypt(spec, listResponseEntity.getBody()));
    }

    @Test
    void shouldDecryptEmptySeatsResponse(){
        // given
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("secrets");
        String seanceUid = "seance-id7";

        // when
        HttpEntity<String> listResponseEntity = sendGetReservedSeatRequest(seanceUid);

        // then
        var decryptedResponse = CryptoTestUtils.decrypt(spec, listResponseEntity.getBody());
        var seats = responseToStringList(decryptedResponse);
        Assertions.assertEquals(0, seats.size());
    }

    @Test
    void shouldDecryptSeatsResponse(){
        // given
        SecretKeySpec spec = CryptoTestUtils.createSecretSpec("secrets");
        String seanceUid = "seance-id2";

        // when
        HttpEntity<String> listResponseEntity = sendGetReservedSeatRequest(seanceUid);

        // then
        var decryptedResponse = CryptoTestUtils.decrypt(spec, listResponseEntity.getBody());
        var seats = responseToStringList(decryptedResponse);
        Assertions.assertEquals(5, seats.size());
    }

    @SneakyThrows
    private ResponseEntity<String> sendGetReservedSeatRequest(String seanceUid) {
        String url = "/api/v1/seance/reserved/" + seanceUid;
        return testRestTemplate.getForEntity(url, String.class);
    }

    private ResponseEntity<String> sendSearchRequest(LocalDateTime after, LocalDateTime before, String type) {
        URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:7000/api/v1/seance")
                .queryParam("after", after)
                .queryParam("before", before)
                .queryParam("type", type)
                .build().encode().toUri();
        return testRestTemplate.exchange(
                uri,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                String.class
        );
    }

    @SneakyThrows
    private List<SeanceDto> responseToSeanceDtoList(String response) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return Arrays.asList(mapper.readValue(response, SeanceDto[].class));
    }

    @SneakyThrows
    private List<String> responseToStringList(String response) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return Arrays.asList(mapper.readValue(response, String[].class));
    }
}
