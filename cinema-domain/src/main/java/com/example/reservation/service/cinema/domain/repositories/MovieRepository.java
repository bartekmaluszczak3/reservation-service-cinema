package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findById(Long id);
}
