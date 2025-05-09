package com.example.reservation.service.cinema.service.service;

import com.example.reservation.service.cinema.domain.model.Reservation;
import com.example.reservation.service.cinema.domain.model.ReservationStatus;
import com.example.reservation.service.cinema.domain.model.Seance;
import com.example.reservation.service.cinema.domain.repositories.ReservationRepository;
import com.example.reservation.service.cinema.service.crypto.encrypter.Encrypter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final Encrypter encrypter;
    private final ObjectMapper mapper;

    public ReservationService(ReservationRepository reservationRepository, Encrypter encrypter) {
        this.reservationRepository = reservationRepository;
        this.encrypter = encrypter;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String createReservation(CreateReservationParams params){
        String reservationUid = UUID.randomUUID().toString();
        Reservation reservation = Reservation.builder()
                .reservedSeats(params.reservedSeats)
                .userUuid(params.userUid)
                .createdTime(LocalDateTime.now())
                .modifiedTime(LocalDateTime.now())
                .seance(params.seanceUid)
                .uuid(reservationUid)
                .reservationStatus(ReservationStatus.ACTIVE)
                .build();
        reservationRepository.save(reservation);
        return reservationUid;
    }

    public record CreateReservationParams(String userUid, List<String> reservedSeats, Seance seanceUid) {
    }
}
