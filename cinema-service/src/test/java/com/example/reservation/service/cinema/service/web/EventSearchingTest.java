package com.example.reservation.service.cinema.service.web;

import com.example.reservation.service.cinema.domain.dto.EventDto;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.DEFINED_PORT, classes = Application.class, properties = {"server.port=7777"})
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class EventSearchingTest {

    @Autowired
    TestRestTemplate testRestTemplate;

    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
        container.initRecords();
    }

    @AfterEach
    void afterEach() throws IOException {
        container.clearRecords();
        container.clearDatabase();
    }

    @Test
    void shouldReturnAllRecordsWhenNonCriteriaIsSpecified() {
        // when
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(null, null, null);

        // then
        var events = listResponseEntity.getBody();
        Assertions.assertEquals(1, events.size());
    }

    @Test
    void shouldReturnAllRecordsForGivenType() {
        // given
        String type = "horror";

        // when
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(null, null, type);

        // then
        var events = listResponseEntity.getBody();
        Assertions.assertEquals(2, events.size());
    }

    @Test
    void shouldReturnAllRecordsBeforeDate() {
        // given
        LocalDateTime beforeDate = LocalDateTime.parse("2004-12-19T10:23:54");

        // when
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(null, beforeDate, null);

        // then
        var events = listResponseEntity.getBody();
        Assertions.assertEquals(2, events.size());
        List<String> movieIds = events.stream().map(EventDto::getMovieUid).toList();
        Assertions.assertTrue(movieIds.contains("movie-id-5"));
        Assertions.assertTrue(movieIds.contains("movie-id"));
    }

    @Test
    void shouldReturnAllRecordsAfterDate(){
        // given
        LocalDateTime after = LocalDateTime.now().plusYears(100);

        // when
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(after, null, null);
        // then

        var events = listResponseEntity.getBody();
        Assertions.assertEquals(2, events.size());
        List<String> eventsId = events.stream().map(EventDto::getEventUuid).toList();
        Assertions.assertTrue(eventsId.contains("event-id7"));
        Assertions.assertTrue(eventsId.contains("event-id8"));
    }

    @Test
    void shouldReturnAllRecordsBetweenDate(){
        // given
        LocalDateTime afterDate = LocalDateTime.parse("2030-09-19T10:23:54");
        LocalDateTime beforeDate = LocalDateTime.parse("2031-01-01T10:20:54");

        // when
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(afterDate, beforeDate, null);

        // then
        var events = listResponseEntity.getBody();
        Assertions.assertEquals(3, events.size());
        List<String> moveIds = events.stream().map(EventDto::getMovieUid).toList();
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
        HttpEntity<List<EventDto>> listResponseEntity = sendRequest(afterDate, beforeDate, type);

        // then
        var events = listResponseEntity.getBody();
        Assertions.assertEquals(2, events.size());
        List<String> moveIds = events.stream().map(EventDto::getMovieUid).toList();
        Assertions.assertTrue(moveIds.contains("movie-id-7"));
        Assertions.assertTrue(moveIds.contains("movie-id-6"));
    }

    private ResponseEntity<List<EventDto>> sendRequest(LocalDateTime after, LocalDateTime before, String type){
        URI uri = UriComponentsBuilder.fromHttpUrl("http://localhost:7777/api/v1/event")
                .queryParam("after", after)
                .queryParam("before", before)
                .queryParam("type", type)
                .build().encode().toUri();
        return testRestTemplate.exchange(
                uri,
                HttpMethod.GET,
                HttpEntity.EMPTY,
                new ParameterizedTypeReference<>() {}
        );
    }

}
