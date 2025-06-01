package com.example.reservation.service.cinema.domain.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "seance")
public class Seance {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String uuid;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @ManyToOne
    @JoinColumn(name = "movie_uid", referencedColumnName = "uuid")
    private Movie movie;

    @ManyToOne
    @JoinColumn(name = "room_uid", referencedColumnName ="uuid")
    private Room room;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "seance", cascade = CascadeType.ALL)
    private List<Reservation> reservationList = new ArrayList<>();

    public Set<String> getReservedSeats(){
        Set<String> reservedSeat = new HashSet<>();
        reservationList.stream()
                .filter(e-> e.getReservationStatus().equals(ReservationStatus.ACTIVE))
                .forEach(e-> reservedSeat.addAll(e.getReservedSeats()));
        return reservedSeat;
    }
}
