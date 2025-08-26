package com.example.reservation.service.cinema.service.feature;

import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.kafka.EventBuilder;
import com.example.reservation.service.cinema.service.kafka.MockConsumer;
import com.example.reservation.service.cinema.service.kafka.MockProducer;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestPropertySource(properties = {"service.crypto.disabled=true"})
@DirtiesContext
@EmbeddedKafka(partitions = 1, controlledShutdown = false)
public class SeatsCancellationTest {
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
        mockConsumer.resetReservationStateChangedFailedEvents();
    }


    @SneakyThrows
    @Test
    void shouldReserveSeatsThatWereCancelled(){
        // given
        String reservationUid = "reservation-id-1";
        String seanceUid = "seance-id3";
        List<String> seats = List.of("1", "2");
        Event cancelEvent = EventBuilder.buildCancelEvent(reservationUid, seanceUid);
        String serializedCancelEvent = new ObjectMapper().writeValueAsString(cancelEvent);

        // when
        mockProducer.sendReservationCancel(serializedCancelEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(15))
                .until(() -> mockConsumer.countReservationStateChangedEvents() == 1);

        var cancelledReservation = getReservation(reservationUid);
        ReservationStatus reservationStatus = ReservationStatus.valueOf(cancelledReservation.get("status").toString());
        Assertions.assertEquals(ReservationStatus.CANCELLED, reservationStatus);

        // and
        Event reserveSeatEvent = EventBuilder.buildReserveSeatsEvent(seanceUid, seats);
        String serializedReserveEvent = new ObjectMapper().writeValueAsString(reserveSeatEvent);

        // when
        mockProducer.sendSeanceReserved(serializedReserveEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.countReservationStateChangedEvents() == 2);

        String newReservationUid = new JSONObject(mockConsumer.getLatestPayloadOfReservationStateChangedEvents())
                .getJSONObject("eventData").getString("reservationUid");
        Assertions.assertNotEquals(newReservationUid, reservationUid);
        var newReservation =  getReservation(newReservationUid);
        ReservationStatus newReservationStatus = ReservationStatus.valueOf(newReservation.get("status").toString());
        Assertions.assertEquals(ReservationStatus.ACTIVE, newReservationStatus);
    }

    private Map<String, Object> getReservation(String reservationUid){
        return container.executeQueryForObjects(String.format("SELECT * from reservation where uuid='%s';", reservationUid)).get(0);
    }
}
