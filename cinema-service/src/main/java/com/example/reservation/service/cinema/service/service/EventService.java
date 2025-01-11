package com.example.reservation.service.cinema.service.service;

import com.example.reservation.service.cinema.domain.dto.EventDto;
import com.example.reservation.service.cinema.domain.model.Event;
import com.example.reservation.service.cinema.domain.repositories.EventRepository;
import com.example.reservation.service.cinema.domain.repositories.EventSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;

    public List<EventDto> getSpecifiedEvents(LocalDateTime beforeTime, LocalDateTime afterTime, String type){
        Specification<Event> specification = EventSpecification.after(afterTime)
                .and(EventSpecification.before(beforeTime))
                .and(EventSpecification.hasType(type));
        var events = eventRepository.findAll(specification);
        return events.stream().
                map(EventDto::fromEntity)
                .collect(Collectors.toList());
    }
}
