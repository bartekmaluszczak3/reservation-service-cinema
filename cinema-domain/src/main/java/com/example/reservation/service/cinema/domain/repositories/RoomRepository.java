package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
}
