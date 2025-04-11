package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.events.events.Event;
import org.example.events.events.eventdata.ReservationStateChangedData;
import org.example.events.events.eventdata.ReserveStateChangeFailedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.RESERVATION_STATE_CHANGED;
import static com.example.reservation.service.cinema.service.kafka.TopicsNames.RESERVATION_STATE_CHANGE_FAILED;

@Service
@Slf4j
public class ProducerService {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public void sendReserveStateChangeFailedEvent(ReserveStateChangeFailedData reserveSeatFailedData) throws JsonProcessingException {
        Event event = Event.builder()
                .eventType("ReserveStateChangeFailedEvent")
                .id(UUID.randomUUID().toString())
                .timestamp(Date.from(Instant.now()))
                .eventData(reserveSeatFailedData)
                .build();
        log.info("Sending ReserveSeatFailedEvent");
        String serializedEvent = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(RESERVATION_STATE_CHANGE_FAILED, serializedEvent);
    }

    public void sendReservationStateChangedEvent(ReservationStateChangedData reservationStateChangedData) throws JsonProcessingException {
        Event event = Event.builder()
                .eventType("ReservationStateChangedDataEvent")
                .id(UUID.randomUUID().toString())
                .timestamp(Date.from(Instant.now()))
                .eventData(reservationStateChangedData)
                .build();
        log.info("Sending ReservationStateChangedDataEvent");
        String serializedEvent = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(RESERVATION_STATE_CHANGED, serializedEvent);

    }
}
