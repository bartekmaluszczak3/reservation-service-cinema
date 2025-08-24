package com.example.reservation.service.cinema.service.controller;

import com.example.reservation.service.cinema.domain.dto.ReservationDto;
import com.example.reservation.service.cinema.service.service.ReservationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.authservice.domain.entity.User;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/reservation")
@RequiredArgsConstructor
@Slf4j
public class ReservationController {
    private final ReservationService reservationService;


    @GetMapping("")
    List<ReservationDto> getReservation(){
        log.info("Received get reservation request");
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            User user = (User) authentication.getPrincipal();
            return reservationService.getReservations(user.getUserUid());
        }catch (Exception e){
            return List.of();
        }

    }
}
