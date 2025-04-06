package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.service.exception.ReserveSeatsFailedException;
import com.example.reservation.service.cinema.service.exception.SeanceNotFoundException;
import com.example.reservation.service.cinema.service.service.SeanceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.events.eventdata.ReservationStateChangedData;
import org.example.events.events.eventdata.ReserveSeatData;
import org.example.events.events.eventdata.ReserveSeatFailedData;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE;


@Component
@Slf4j
public class ConsumerService {

    private final SeanceService seanceService;

    private final ProducerService producerService;

    public ConsumerService(SeanceService seanceService, ProducerService producerService) {
        this.seanceService = seanceService;
        this.producerService = producerService;
    }

    @KafkaListener(topics = SEANCE_RESERVE)
    public void seanceReserve(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        log.info("Received SeanceReserveEvent");
        String jsonEvent = consumerRecord.value().toString();
        log.debug("Event {}", jsonEvent);
        ReserveSeatData reserveSeatData = EventParser.parseEvent(jsonEvent);
        try {
            String reservationUid = seanceService.reserveSeat(reserveSeatData.getSeanceUid(), reserveSeatData.getUserUid(), reserveSeatData.getReservedSeat());
            producerService.sendReservationStateChangedEvent(
                    ReservationStateChangedData.builder()
                            .reservationUid(reservationUid)
                            .oldStatus(null)
                            .newStatus(ReservationStatus.ACTIVE.name())
                            .build()
            );
        } catch (SeanceNotFoundException | ReserveSeatsFailedException e) {
            producerService.sendReserveFailedEvent(
                    ReserveSeatFailedData.builder()
                            .reason(e.getMessage())
                            .reservedSeat(reserveSeatData.getReservedSeat())
                            .seanceUid(reserveSeatData.getSeanceUid())
                            .build()
            );
        }
    }
}
