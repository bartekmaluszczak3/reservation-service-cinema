package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Seance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SeanceRepository extends JpaRepository<Seance, Long>, JpaSpecificationExecutor<Seance> {
    Optional<String> findByUuid(String id);

}
