package com.example.reservation.service.cinema.domain.dto;

import com.example.reservation.service.cinema.domain.model.Seance;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class SeanceDto {
    private String seanceUuid;
    private LocalDateTime startTime;
    private Set<String> reservedSeat;
    private String movieTitle;
    private String movieUid;
    private String roomName;
    private int roomCapacity;

    public static SeanceDto fromEntity(Seance event){
        return SeanceDto.builder()
                .seanceUuid(event.getUuid())
                .startTime(event.getStartTime())
                .reservedSeat(event.getReservedSeats())
                .movieTitle(event.getMovie().getTitle())
                .movieUid(event.getMovie().getUuid())
                .roomName(event.getRoom().getName())
                .roomCapacity(event.getRoom().getCapacity())
                .build();
    }
}
