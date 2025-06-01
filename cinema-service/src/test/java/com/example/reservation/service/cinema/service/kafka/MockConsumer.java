package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.*;

@Component
public class MockConsumer {

    private final String GROUP_ID = "MOCK_ID";
    private final HashMap<Integer, String> seanceReserveEvents = new HashMap<>();
    private final HashMap<Integer, String> reservationStateChangedEvents = new HashMap<>();
    private final HashMap<Integer, String> reservationStateChangedFailedEvents = new HashMap<>();

    @KafkaListener(topics = SEANCE_RESERVE, groupId = GROUP_ID)
    public void seanceReserved(ConsumerRecord<?, ?> consumerRecord) {
        String payload = consumerRecord.value().toString();
        int size = seanceReserveEvents.size() + 1;
        seanceReserveEvents.put(size, payload);
    }

    @KafkaListener(topics = RESERVATION_STATE_CHANGED, groupId = GROUP_ID)
    public void reservationStateChanged(ConsumerRecord<?, ?> consumerRecord){
        String payload = consumerRecord.value().toString();
        int size = reservationStateChangedEvents.size() + 1;
        reservationStateChangedEvents.put(size, payload);
    }

    @KafkaListener(topics = RESERVATION_STATE_CHANGE_FAILED, groupId = GROUP_ID)
    public void reserveStatChangeFailed(ConsumerRecord<?, ?> consumerRecord) {
        String payload = consumerRecord.value().toString();
        int size = reservationStateChangedFailedEvents.size() + 1;
        reservationStateChangedFailedEvents.put(size, payload);
    }

    public int countSeanceReservedEvents(){
        return seanceReserveEvents.size();
    }

    public int countReservationStateChangedEvents(){
        return reservationStateChangedEvents.size();
    }

    public int countReservationStateChangedFailedEvents(){
        return reservationStateChangedFailedEvents.size();
    }

    public String getLatestPayloadOfReserveEvent(){
        return seanceReserveEvents.get(seanceReserveEvents.size());
    }

    public String getLatestPayloadOfReservationStateChangedEvents(){
        return reservationStateChangedEvents.get(reservationStateChangedEvents.size());
    }

    public String getLatestPayloadOfReservationStateChangedFailedEvents(){
        return reservationStateChangedFailedEvents.get(reservationStateChangedFailedEvents.size());
    }

    public void resetSeanceReservedEvents(){
        this.seanceReserveEvents.clear();
    }

    public void resetReservationStateChangedEvents(){
        this.reservationStateChangedEvents.clear();
    }

    public void resetReservationStateChangedFailedEvents(){
        this.reservationStateChangedFailedEvents.clear();
    }
}
