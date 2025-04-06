package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.example.events.events.eventdata.EventData;
import org.example.events.events.eventdata.ReserveSeatData;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class SeanceReserveFailedTest {

    private static final PostgresContainer container = new PostgresContainer();

    @Autowired
    MockProducer mockProducer;

    @Autowired
    MockConsumer mockConsumer;


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
    void shouldSendFailedEventWhenSeanceUidIsNotFound(){
        // given
        String seanceUid = "invalid-uid";
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
                .until(() -> mockConsumer.getPayload() != null);
        String payload = mockConsumer.getPayload();
        Assertions.assertTrue(payload.contains("ReserveSeatFailedEvent"));
        Assertions.assertTrue(payload.contains(seanceUid));
        Assertions.assertTrue(payload.contains(String.format("Seance with uid %s not found", seanceUid)));
    }

    @SneakyThrows
    @Test
    void shouldSendFailedEventWhenSeatsAreCurrentlyReserved(){
        String seanceUid = "seance-id1";
        List<String> reservedSeat = List.of("das", "13","1");
        Event event = EventBuilder.buildEvent("ReserveSeatEvent", seanceUid, reservedSeat);


        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);
        String payload = mockConsumer.getPayload();
        Assertions.assertTrue(payload.contains("ReserveSeatFailedEvent"));
        Assertions.assertTrue(payload.contains(seanceUid));
        Assertions.assertTrue(payload.contains("These seats are currently taken"));
    }
}
