package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByUuid(String id);
}
