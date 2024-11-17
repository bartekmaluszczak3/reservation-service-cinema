package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

}
