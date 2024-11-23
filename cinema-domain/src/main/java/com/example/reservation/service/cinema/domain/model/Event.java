package com.example.reservation.service.cinema.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "event")
public class Event {
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

    @Column(name = "reserved_seats")
    private Set<String> reservedSeats = new HashSet<>();

    public boolean reserveSeat(String seatNumber){
        return this.reservedSeats.add(seatNumber);
    }

    public boolean cancelSeat(String seatNumber){
        return this.reservedSeats.remove(seatNumber);
    }

}
