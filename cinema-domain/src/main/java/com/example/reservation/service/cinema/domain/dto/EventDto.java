package com.example.reservation.service.cinema.domain.dto;

import com.example.reservation.service.cinema.domain.model.Event;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
public class EventDto {
    private String eventUuid;
    private LocalDateTime startTime;
    private Set<String> reservedSeat;
    private String movieTitle;
    private String movieUid;
    private String roomName;
    private int roomCapacity;

    public static EventDto fromEntity(Event event){
        return EventDto.builder()
                .eventUuid(event.getUuid())
                .startTime(event.getStartTime())
                .reservedSeat(event.getReservedSeats())
                .movieTitle(event.getMovie().getTitle())
                .movieUid(event.getMovie().getUuid())
                .roomName(event.getRoom().getName())
                .roomCapacity(event.getRoom().getCapacity())
                .build();
    }
}
