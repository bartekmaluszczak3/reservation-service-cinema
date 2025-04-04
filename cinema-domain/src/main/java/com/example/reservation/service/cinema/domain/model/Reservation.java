package com.example.reservation.service.cinema.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "reservation")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String uuid;

    @Column(name = "reservation_date")
    private LocalDateTime reservationDate;

    @Column(name = "user_uid")
    private String userUuid;

    @ManyToOne
    @JoinColumn(name = "seance_uid", referencedColumnName ="uuid")
    private Seance seance;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    @Column(name = "reserved_seats")
    private List<String> reservedSeats = new ArrayList<>();

}
