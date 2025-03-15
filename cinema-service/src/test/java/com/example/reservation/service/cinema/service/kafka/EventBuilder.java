package com.example.reservation.service.cinema.service.kafka;

import org.example.events.events.Event;
import org.example.events.events.eventdata.EventData;
import org.example.events.events.eventdata.ReserveSeatData;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class EventBuilder {

    public static Event buildEvent(String type, String seanceUid, List<String> reservedSeats){
        EventData eventData = ReserveSeatData.builder()
                .seanceUid(seanceUid)
                .reservedSeat(reservedSeats)
                .build();

        return Event.builder()
                .id(UUID.randomUUID().toString())
                .eventType(type)
                .timestamp(Date.from(Instant.now()))
                .eventData(eventData)
                .build();
    }
}
