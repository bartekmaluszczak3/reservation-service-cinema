package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false)
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

    @AfterEach
    void afterEach(){
        mockConsumer.resetReservationStateChangedFailedEvents();
    }

    @SneakyThrows
    @Test
    void shouldSendFailedEventWhenSeanceUidIsNotFound(){
        // given
        String seanceUid = "invalid-uid";
        List<String> reservedSeat = List.of("112", "113");

        Event event = EventBuilder.buildReserveSeatsEvent(seanceUid, reservedSeat);

        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.countReservationStateChangedFailedEvents() == 1);
        String payload = mockConsumer.getLatestPayloadOfReservationStateChangedFailedEvents();
        Assertions.assertTrue(payload.contains("ReserveStateChangeFailedEvent"));
        Assertions.assertTrue(payload.contains(seanceUid));
        Assertions.assertTrue(payload.contains(String.format("Seance with uid %s not found", seanceUid)));
    }

    @SneakyThrows
    @Test
    void shouldSendFailedEventWhenSeatsAreCurrentlyReserved(){
        String seanceUid = "seance-id3";
        List<String> reservedSeat = List.of("das", "13","1");
        Event event = EventBuilder.buildReserveSeatsEvent(seanceUid, reservedSeat);

        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.countReservationStateChangedFailedEvents() == 1);
        String payload = mockConsumer.getLatestPayloadOfReservationStateChangedFailedEvents();
        Assertions.assertTrue(payload.contains("ReserveStateChangeFailedEvent"));
        Assertions.assertTrue(payload.contains(seanceUid));
        Assertions.assertTrue(payload.contains("These seats are currently taken"));
    }
}
