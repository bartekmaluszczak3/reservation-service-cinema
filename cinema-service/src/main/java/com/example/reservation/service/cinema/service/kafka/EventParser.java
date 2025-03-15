package com.example.reservation.service.cinema.service.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.events.events.eventdata.ReserveSeatData;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventParser {
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static ReserveSeatData parseEvent(String jsonEvent) throws JsonProcessingException {
        jsonEvent = jsonEvent.substring(1, jsonEvent.length() - 1);
        var str = jsonEvent.replace("\\","");
        var map = new ObjectMapper().readValue(str, HashMap.class);
        var eventData = map.get("eventData");
        Map<String, Object> eventDataMap = (Map<String, Object>) eventData;
        List<String> reservedSeats = (List<String>) eventDataMap.get("reservedSeat");

        return ReserveSeatData.builder()
                .reservedSeat(reservedSeats)
                .seanceUid(eventDataMap.get("seanceUid").toString())
                .build();
    }
}
