package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.RESERVATION_STATE_CHANGED;
import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE;

@Component
public class MockConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload = null;

    @KafkaListener(topics = SEANCE_RESERVE)
    public void seanceReserved(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        payload = consumerRecord.value().toString();
        latch.countDown();
    }

    @KafkaListener(topics = RESERVATION_STATE_CHANGED)
    public void reservationStateChanged(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        payload = consumerRecord.value().toString();
        latch.countDown();
    }

    public String  getPayload(){
        return payload;
    }

    public void resetPayload(){
        payload = null;
    }

}
