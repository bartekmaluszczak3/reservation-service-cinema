package com.example.reservation.service.cinema.service.kafka;

import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.service.exception.DataIntegrityException;
import com.example.reservation.service.cinema.service.exception.ReservationNotFoundException;
import com.example.reservation.service.cinema.service.exception.ReserveSeatsFailedException;
import com.example.reservation.service.cinema.service.exception.SeanceNotFoundException;
import com.example.reservation.service.cinema.service.service.ReservationService;
import com.example.reservation.service.cinema.service.service.SeanceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.example.events.events.eventdata.CancelReservationData;
import org.example.events.events.eventdata.ReservationStateChangedData;
import org.example.events.events.eventdata.ReserveSeatData;
import org.example.events.events.eventdata.ReserveStateChangeFailedData;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import static com.example.reservation.service.cinema.service.kafka.TopicsNames.CANCEL_RESERVATION;
import static com.example.reservation.service.cinema.service.kafka.TopicsNames.SEANCE_RESERVE;


@Component
@Slf4j
public class ConsumerService {

    private final SeanceService seanceService;

    private final ProducerService producerService;

    private final ReservationService reservationService;


    public ConsumerService(SeanceService seanceService, ProducerService producerService, ReservationService reservationService) {
        this.seanceService = seanceService;
        this.producerService = producerService;
        this.reservationService = reservationService;
    }

    @KafkaListener(topics = SEANCE_RESERVE)
    public void seanceReserve(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        log.info("Received SeanceReserveEvent");
        String jsonEvent = consumerRecord.value().toString();
        log.debug("Event {}", jsonEvent);
        ReserveSeatData reserveSeatData = EventParser.parseReserveSeatData(jsonEvent);
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
            producerService.sendReserveStateChangeFailedEvent(
                    ReserveStateChangeFailedData.builder()
                            .reason(e.getMessage())
                            .reservedSeat(reserveSeatData.getReservedSeat())
                            .oldState(null)
                            .newState(ReservationStatus.ACTIVE.name())
                            .seanceUid(reserveSeatData.getSeanceUid())
                            .build()
            );
        }
    }

    @KafkaListener(topics = CANCEL_RESERVATION)
    public void cancelReservation(ConsumerRecord<?, ?> consumerRecord) throws JsonProcessingException {
        log.info("Received CancelReservationEvent");
        String jsonEvent = consumerRecord.value().toString();
        log.debug("Event {}", jsonEvent);
        CancelReservationData cancelReservationData = EventParser.parseCancelReservationData(jsonEvent);
        try {
            reservationService.cancelReservation(new ReservationService.CancelReservationParams(cancelReservationData.getReservationUuid(),
                    cancelReservationData.getSeanceUid()));
            producerService.sendReservationStateChangedEvent(
                    ReservationStateChangedData.builder()
                            .reservationUid(cancelReservationData.getReservationUuid())
                            .oldStatus(ReservationStatus.ACTIVE.name())
                            .newStatus(ReservationStatus.CANCELLED.name())
                            .build());
        } catch (ReservationNotFoundException | DataIntegrityException e) {
            log.error(e.getMessage());
            producerService.sendReserveStateChangeFailedEvent(
                    ReserveStateChangeFailedData.builder()
                            .reservationUid(cancelReservationData.getReservationUuid())
                            .reason(e.getMessage())
                            .reservedSeat(null)
                            .oldState(ReservationStatus.ACTIVE.name())
                            .newState(ReservationStatus.CANCELLED.name())
                            .seanceUid(cancelReservationData.getSeanceUid())
                            .build());
        }

    }
}
