package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE;

@Component
public class MockProducer {
    @Autowired
    KafkaTemplate<String, String> kafkaTemplate;

    ObjectMapper objectMapper = new ObjectMapper().setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);

    public void sendSeanceReserved(String seanceReserve) throws JsonProcessingException {
        String jsonEvent = objectMapper.writeValueAsString(seanceReserve);
        kafkaTemplate.send(SEANCE_RESERVE, jsonEvent);
    }

}
