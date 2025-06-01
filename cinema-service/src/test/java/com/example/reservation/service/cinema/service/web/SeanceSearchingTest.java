package com.example.reservation.service.cinema.service.web;

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

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7777"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestPropertySource(properties = {"service.crypto.disabled=true"})
public class SeanceSearchingTest {

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
    void shouldReturnAllRecordsWhenNonCriteriaIsSpecified() {
        // when
        HttpEntity<String> listResponseEntity = sendRequest(null, null, null);

        // then
        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(11, seances.size());
    }

    @Test
    void shouldReturnAllRecordsForGivenType() {
        // given
        String type = "horror";

        // when
        HttpEntity<String> listResponseEntity = sendRequest(null, null, type);

        // then
        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(5, seances.size());
    }

    @Test
    void shouldReturnAllRecordsBeforeDate() {
        // given
        LocalDateTime beforeDate = LocalDateTime.parse("2004-12-19T10:23:54");

        // when
        HttpEntity<String> listResponseEntity = sendRequest(null, beforeDate, null);

        // then
        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(2, seances.size());
        List<String> movieIds = seances.stream().map(SeanceDto::getMovieUid).toList();
        Assertions.assertTrue(movieIds.contains("movie-id-5"));
        Assertions.assertTrue(movieIds.contains("movie-id"));
    }

    @Test
    void shouldReturnAllRecordsAfterDate(){
        // given
        LocalDateTime after = LocalDateTime.now().plusYears(100);

        // when
        HttpEntity<String> listResponseEntity = sendRequest(after, null, null);
        // then

        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(2, seances.size());
        List<String> seanceId = seances.stream().map(SeanceDto::getSeanceUuid).toList();
        Assertions.assertTrue(seanceId.contains("seance-id7"));
        Assertions.assertTrue(seanceId.contains("seance-id8"));
    }

    @Test
    void shouldReturnAllRecordsBetweenDate(){
        // given
        LocalDateTime afterDate = LocalDateTime.parse("2030-09-19T10:23:54");
        LocalDateTime beforeDate = LocalDateTime.parse("2031-01-01T10:20:54");

        // when
        HttpEntity<String> listResponseEntity = sendRequest(afterDate, beforeDate, null);

        // then
        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(3, seances.size());
        List<String> moveIds = seances.stream().map(SeanceDto::getMovieUid).toList();
        Assertions.assertTrue(moveIds.contains("movie-id-2"));
        Assertions.assertTrue(moveIds.contains("movie-id-6"));
        Assertions.assertTrue(moveIds.contains("movie-id-7"));
    }

    @Test
    void shouldReturnAllRecordsBetweenDateAndWithGivenType(){
        // given
        LocalDateTime afterDate = LocalDateTime.parse("2030-09-19T10:23:54");
        LocalDateTime beforeDate = LocalDateTime.parse("2031-01-01T10:20:54");
        String type = "thriller";

        // when
        ResponseEntity<String> listResponseEntity = sendRequest(afterDate, beforeDate, type);

        // then
        var seances = responseToList(listResponseEntity.getBody());
        Assertions.assertEquals(2, seances.size());
        List<String> moveIds = seances.stream().map(SeanceDto::getMovieUid).toList();
        Assertions.assertTrue(moveIds.contains("movie-id-7"));
        Assertions.assertTrue(moveIds.contains("movie-id-6"));
    }

    private ResponseEntity<String> sendRequest(LocalDateTime after, LocalDateTime before, String type){
        URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:7777/api/v1/seance")
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
    private List<SeanceDto> responseToList(String response){
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return Arrays.asList(mapper.readValue(response, SeanceDto[].class));
    }
}
