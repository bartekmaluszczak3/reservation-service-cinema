package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.junit.jupiter.api.*;
import org.postgresql.jdbc.PgArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.JsonNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.time.Duration;
import java.util.*;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1)
public class SeanceReserveTest {

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

    @BeforeEach
    void beforeEach(){
        mockConsumer.resetReservationStateChangedEvents();
    }

    @SneakyThrows
    @Test
    void shouldReserveNotOccupiedSeats(){
        // given
        String seanceUid = "seance-id2";
        List<String> reservedSeat = List.of("112", "113");
        Event event = EventBuilder.buildReserveSeatsEvent(seanceUid, reservedSeat);


        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.countReservationStateChangedEvents() == 1);

        String payload = mockConsumer.getLatestPayloadOfReservationStateChangedEvents();
        JsonNode reservationData = new ObjectMapper().readTree(payload).get("eventData");
        Assertions.assertEquals("ACTIVE", (reservationData.get("newStatus").asText()));
        String reservationUid = reservationData.get("reservationUid").asText();

        // and
        var persistedReservation = getReservation(reservationUid);
        Assertions.assertEquals(seanceUid, persistedReservation.get("seance_uid").toString());
        PgArray pgArray = (PgArray) persistedReservation.get("reserved_seats");
        List<String> persistedSeats = Arrays.stream((String[]) pgArray.getArray()).toList();
        Assertions.assertTrue(persistedSeats.containsAll(reservedSeat));
    }

    @SneakyThrows
    @Test
    void shouldNotReserveOccupiedSeat() {
        // given
        String seanceUid = "seance-id3";
        List<String> reservedSeat = List.of("das", "13","1");
        Event event = EventBuilder.buildReserveSeatsEvent(seanceUid, reservedSeat);

        // when
        String eventString = new ObjectMapper().writeValueAsString(event);
        mockProducer.sendSeanceReserved(eventString);

        // then
        var reservedSeatsInSeance = getReservedSeat(seanceUid);
        Assertions.assertFalse(reservedSeatsInSeance.containsAll(reservedSeat));

        // and
        int stateChangedEvents = mockConsumer.countReservationStateChangedEvents();
        Assertions.assertEquals(0, stateChangedEvents);
    }

    private Map<String, Object> getReservation(String reservationUid){
        return container.executeQueryForObjects(String.format("SELECT * from reservation where uuid='%s';", reservationUid)).get(0);
    }

    private List<String> getReservedSeat(String seanceUid){
        List<String> allReservedSeatInSeance = new ArrayList<>();
        List<Map<String, Object>> reservation =
                container.executeQueryForObjects(String.format("SELECT reserved_seats from reservation  where seance_uid='%s';", seanceUid));
        reservation.forEach(e->{
            PgArray pgArray = (PgArray) e.get("reserved_seats");
            try {
                allReservedSeatInSeance.addAll(Arrays.stream((String[]) pgArray.getArray()).toList());
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        });
        return allReservedSeatInSeance;
    }
}
