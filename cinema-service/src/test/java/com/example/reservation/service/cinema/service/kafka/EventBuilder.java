package com.example.reservation.service.cinema.service.kafka;

import org.example.events.events.Event;
import org.example.events.events.eventdata.CancelReservationData;
import org.example.events.events.eventdata.EventData;
import org.example.events.events.eventdata.ReserveSeatData;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventBuilder {

    public static Event buildReserveSeatsEvent(String seanceUid, List<String> reservedSeats){
        EventData eventData = ReserveSeatData.builder()
                .seanceUid(seanceUid)
                .userUid("UserUid")
                .reservedSeat(reservedSeats)
                .build();

        return Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType("ReserveSeatEvent")
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();
    }

    public static Event buildCancelEvent(String reservationUid, String seanceUid){
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
