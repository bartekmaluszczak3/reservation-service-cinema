package com.example.reservation.service.cinema.service.service;

import com.example.reservation.service.cinema.domain.dto.SeanceDto;
import com.example.reservation.service.cinema.domain.model.Seance;
import com.example.reservation.service.cinema.domain.repositories.SeanceRepository;
import com.example.reservation.service.cinema.domain.repositories.SeanceSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SeanceService {

    private final SeanceRepository seanceRepository;

    public List<SeanceDto> getSpecifiedSeance(LocalDateTime beforeTime, LocalDateTime afterTime, String type){
        Specification<Seance> specification = SeanceSpecification.after(afterTime)
                .and(SeanceSpecification.before(beforeTime))
                .and(SeanceSpecification.hasType(type));
        var events = seanceRepository.findAll(specification);
        return events.stream().
                map(SeanceDto::fromEntity)
                .collect(Collectors.toList());
    }
}
