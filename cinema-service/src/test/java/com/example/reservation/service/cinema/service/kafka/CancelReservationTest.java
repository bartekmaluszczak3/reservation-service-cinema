package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.example.events.events.Event;
import org.example.events.events.eventdata.CancelReservationData;
import org.example.events.events.eventdata.EventData;
import org.json.JSONObject;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import static org.testcontainers.shaded.org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ContextConfiguration(initializers = PostgresContainer.class)
@DirtiesContext
@EmbeddedKafka(partitions = 1, brokerProperties = { "listeners=PLAINTEXT://localhost:9092", "port=9092" })
public class CancelReservationTest {

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
    void shouldCancelReservation(){
        // given
        String reservationUid = "reservation-id-1";
        String seanceUid = "seance-id3";
        Event event = createCancelEvent(reservationUid, seanceUid);
        String serializedEvent = new ObjectMapper().writeValueAsString(event);

        // when
        mockProducer.sendReservationCancel(serializedEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);

        var persistedData = getReservation(reservationUid);
        Timestamp modifiedDate =   Timestamp.valueOf(persistedData.get("modified_date").toString());
        Timestamp createdDate = Timestamp.valueOf(persistedData.get("created_date").toString());
        ReservationStatus reservationStatus = ReservationStatus.valueOf(persistedData.get("status").toString());
        Assertions.assertTrue(createdDate.before(modifiedDate));

        Assertions.assertEquals(ReservationStatus.CANCELLED, reservationStatus);
    }

    @SneakyThrows
    @Test
    void shouldNotCancelNonExistingReservation(){
        String nonExistedReservationUid = "dummy-reservation";
        String seanceUid = "seance-id3";
        Event event = createCancelEvent(nonExistedReservationUid, seanceUid);
        String serializedEvent = new ObjectMapper().writeValueAsString(event);

        // when
        mockProducer.sendReservationCancel(serializedEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);

        JSONObject eventData = new JSONObject(mockConsumer.getPayload()).getJSONObject("eventData");
        Assertions.assertEquals(seanceUid, eventData.get("seanceUid"));
        Assertions.assertEquals(nonExistedReservationUid, eventData.get("reservationUid"));
    }

    @SneakyThrows
    @Test
    void shouldNotCancelReservationFromAnotherSeance(){
        // given
        String reservationUid = "reservation-id-2";
        String differentSeance = "seance-id1";
        Event event = createCancelEvent(reservationUid, differentSeance);
        String serializedEvent = new ObjectMapper().writeValueAsString(event);

        // when
        mockProducer.sendReservationCancel(serializedEvent);

        // then
        await()
                .atMost(Duration.ofSeconds(5))
                .until(() -> mockConsumer.getPayload() != null);

        JSONObject eventData = new JSONObject(mockConsumer.getPayload()).getJSONObject("eventData");
        Assertions.assertEquals(differentSeance, eventData.get("seanceUid"));
        Assertions.assertEquals(reservationUid, eventData.get("reservationUid"));

        // and
        var persistedData = getReservation(reservationUid);
        ReservationStatus reservationStatus = ReservationStatus.valueOf(persistedData.get("status").toString());
        Assertions.assertEquals(ReservationStatus.ACTIVE, reservationStatus);

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
