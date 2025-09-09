package com.example.reservation.service.cinema.domain.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "cinema")
public class Cinema {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    private String uuid;

    @Column(name = "city")
    private String city;

    @Column(name = "street")
    private String street;

    @Column(name="cinema_chain")
    private CinemaChain cinemaChain;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "cinema", cascade = CascadeType.ALL)
    private List<Room> rooms = new ArrayList<>();

}
