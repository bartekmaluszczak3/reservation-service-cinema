package com.example.reservation.service.cinema.service.feature;

import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.kafka.MockConsumer;
import com.example.reservation.service.cinema.service.kafka.MockProducer;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.example.events.events.eventdata.CancelReservationData;
import org.example.events.events.eventdata.EventData;
import org.example.events.events.eventdata.ReserveSeatData;
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
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestPropertySource(properties = {"service.crypto.disabled=true"})
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
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
        mockConsumer.resetPayload();
    }


    @SneakyThrows
    @Test
    void shouldReserveSeatsThatWereCancelled(){
        // given
        String reservationUid = "reservation-id-1";
        String seanceUid = "seance-id3";
        List<String> seats = List.of("1", "2");
        Event cancelEvent = createCancelEvent(reservationUid, seanceUid);
        String serializedCancelEvent = new ObjectMapper().writeValueAsString(cancelEvent);

        // when
        mockProducer.sendReservationCancel(serializedCancelEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);
        mockConsumer.resetPayload();

        var persistedData = getReservation(reservationUid);
        ReservationStatus reservationStatus = ReservationStatus.valueOf(persistedData.get("status").toString());
        Assertions.assertEquals(ReservationStatus.CANCELLED, reservationStatus);

        // and
        Event reserveSeatEvent = createReserveSeatEvent(seanceUid, seats);
        String serializedReserveEvent = new ObjectMapper().writeValueAsString(reserveSeatEvent);

        // when
        mockProducer.sendSeanceReserved(serializedReserveEvent);

        // then
        Thread.sleep(10000);
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);

        String newReservationUid =
                new JSONObject(mockConsumer.getPayload()).getJSONObject("eventData").getString("reservationUid");
        System.out.println(newReservationUid);

    }

    private Event createReserveSeatEvent(String seanceUid, List<String> seats){
        EventData eventData = ReserveSeatData.builder()
                .seanceUid(seanceUid)
                .userUid("userUid")
                .reservedSeat(seats)
                .build();

        return Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType("ReserveSeatEvent")
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();

    }


    private Map<String, Object> getReservation(String reservationUid){
        return container.executeQueryForObjects(String.format("SELECT * from reservation where uuid='%s';", reservationUid)).get(0);
    }

    private Event createCancelEvent(String reservationUid, String seanceUid){
        EventData eventData = CancelReservationData.builder()
                .seanceUid(seanceUid)
                .reservationUuid(reservationUid)
                .build();

        return Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType("ReservationCancel")
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();
    }
}
