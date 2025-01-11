package com.example.reservation.service.cinema.service.controller;

import com.example.reservation.service.cinema.domain.dto.EventDto;
import com.example.reservation.service.cinema.service.service.EventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;


@RestController
@RequestMapping("/api/v1/event")
@RequiredArgsConstructor
@Slf4j
public class EventController {
    private final EventService eventService;

    @GetMapping("")
    public List<EventDto> getEvents(@RequestParam(name = "after", required = false) LocalDateTime after,
                                    @RequestParam(name = "before", required = false) LocalDateTime before,
                                    @RequestParam(name = "type", required = false) String type){
        log.info("Received search events request");
        log.debug("After = {}, Before = {}, type = {}", after, before, type);
        return eventService.getSpecifiedEvents(before, after, type);
    }
}
