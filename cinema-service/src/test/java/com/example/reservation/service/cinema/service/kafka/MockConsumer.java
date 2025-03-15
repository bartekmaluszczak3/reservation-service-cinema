package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE_FAILED;

@Component
public class MockConsumer {

    private CountDownLatch latch = new CountDownLatch(1);
    private String payload;

    @KafkaListener(topics = SEANCE_RESERVE_FAILED)
    public void listen(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        payload = consumerRecord.value().toString();
        latch.countDown();
    }

    public void resetLatch() {
        latch = new CountDownLatch(1);
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public String  getPayload(){
        return payload;
    }

}
