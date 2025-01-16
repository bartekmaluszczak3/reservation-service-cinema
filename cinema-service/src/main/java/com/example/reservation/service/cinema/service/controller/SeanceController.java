package com.example.reservation.service.cinema.service.controller;

import com.example.reservation.service.cinema.domain.dto.SeanceDto;
import com.example.reservation.service.cinema.service.exception.SeanceNotFoundException;
import com.example.reservation.service.cinema.service.service.SeanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/v1/seance")
@RequiredArgsConstructor
@Slf4j
public class SeanceController {
    private final SeanceService seanceService;

    @GetMapping("")
    public List<SeanceDto> getEvents(@RequestParam(name = "after", required = false) LocalDateTime after,
                                     @RequestParam(name = "before", required = false) LocalDateTime before,
                                     @RequestParam(name = "type", required = false) String type){
        log.info("Received search events request");
        log.debug("After = {}, Before = {}, type = {}", after, before, type);
        return seanceService.getSpecifiedSeance(before, after, type);
    }

    @GetMapping("/reserved/{uid}")
    public List<String> getReservedSeats(@PathVariable String uid) throws SeanceNotFoundException {
        log.info("Received get reserved");
        return seanceService.getReservedSeats(uid);
    }
}
