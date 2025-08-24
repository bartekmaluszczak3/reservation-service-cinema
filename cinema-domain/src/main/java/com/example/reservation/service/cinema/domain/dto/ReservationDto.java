package com.example.reservation.service.cinema.domain.dto;

import com.example.reservation.service.cinema.domain.model.Reservation;
import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Builder
@Getter
@RequiredArgsConstructor
@AllArgsConstructor
public class ReservationDto {
    private String uuid;
    private String seanceUuid;
    private ReservationStatus reservationStatus;
    private List<String> reservedSeats;

    public static ReservationDto fromEntity(Reservation reservation){
        return ReservationDto.builder()
                .uuid(reservation.getUuid())
                .reservationStatus(reservation.getReservationStatus())
                .seanceUuid(reservation.getSeance().getUuid())
                .reservedSeats(reservation.getReservedSeats())
                .build();


    }
}
