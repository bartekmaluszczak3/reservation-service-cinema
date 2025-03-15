package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.example.events.events.eventdata.EventData;
import org.example.events.events.eventdata.ReserveSeatData;
import org.junit.jupiter.api.*;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.org.awaitility.core.ConditionTimeoutException;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class SeanceReserveTest {

    private static final PostgresContainer container = new PostgresContainer();

    @Autowired
    MockProducer mockProducer;

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
    void shouldReserveNotOccupiedSeats(){
        // given
        String seanceUid = "seance-id2";
        List<String> reservedSeat = List.of("112", "113");
        EventData eventData = ReserveSeatData.builder()
                .seanceUid(seanceUid)
                .reservedSeat(reservedSeat)
                .build();

        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType("ReserveSeatEvent")
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();

        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(()-> waitForProcessEvent(reservedSeat, seanceUid));
    }

    @SneakyThrows
    @Test
    void shouldNotReserveOccupiedSeat() {
        // given
        String seanceUid = "seance-id1";
        List<String> reservedSeat = List.of("das", "13","1");

        EventData eventData = ReserveSeatData.builder()
                .seanceUid(seanceUid)
                .reservedSeat(reservedSeat)
                .build();

        Event event = Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType("ReserveSeatEvent")
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();
        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        Assertions.assertThrows(ConditionTimeoutException.class, ()->
                await()
                .atMost(Duration.ofSeconds(5))
                .until(()-> waitForProcessEvent(reservedSeat, seanceUid)));
    }

    private boolean waitForProcessEvent(List<String> reservedSeats, String seanceUid) throws SQLException {
        Map<String, Object> maps = container.executeQueryForObjects(String.format("SELECT reserved_seats from seance where uuid='%s';", seanceUid)).get(0);
        PgArray pgArray = (PgArray) maps.get("reserved_seats");
        List<String> array = Arrays.stream((String[]) pgArray.getArray()).toList();
        return array.containsAll(reservedSeats);
    }
}
