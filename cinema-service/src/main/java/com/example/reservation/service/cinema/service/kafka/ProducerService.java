package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.example.events.events.Event;
import org.example.events.events.eventdata.ReserveSeatFailedData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE_FAILED;

@Service
@Slf4j
public class ProducerService {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;
    ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public void sendReserveFailedEvent(ReserveSeatFailedData reserveSeatFailedData) throws JsonProcessingException {
        Event event = Event.builder()
                .eventType("ReserveSeatFailedEvent")
                .id(UUID.randomUUID().toString())
                .timestamp(Date.from(Instant.now()))
                .eventData(reserveSeatFailedData)
                .build();
        log.info("Sending ReserveSeatFailedEvent");
        String serializedEvent = objectMapper.writeValueAsString(event);
        kafkaTemplate.send(SEANCE_RESERVE_FAILED, serializedEvent);
    }
}
