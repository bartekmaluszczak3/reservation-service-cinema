package com.example.reservation.service.cinema.service.service;

import com.example.reservation.service.cinema.domain.dto.SeanceDto;
import com.example.reservation.service.cinema.domain.model.Seance;
import com.example.reservation.service.cinema.domain.repositories.SeanceRepository;
import com.example.reservation.service.cinema.domain.repositories.SeanceSpecification;
import com.example.reservation.service.cinema.service.crypto.encrypter.Encrypter;
import com.example.reservation.service.cinema.service.exception.EncryptFailed;
import com.example.reservation.service.cinema.service.exception.ReserveSeatsFailedException;
import com.example.reservation.service.cinema.service.exception.SeanceNotFoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SeanceService {

    private final SeanceRepository seanceRepository;

    private final Encrypter encrypter;

    private final ObjectMapper mapper;

    public SeanceService(SeanceRepository seanceRepository, Encrypter encrypter){
        this.seanceRepository = seanceRepository;
        this.encrypter = encrypter;
        this.mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    public String getSpecifiedSeance(LocalDateTime beforeTime, LocalDateTime afterTime, String type) throws EncryptFailed {
        Specification<Seance> specification = SeanceSpecification.after(afterTime)
                .and(SeanceSpecification.before(beforeTime))
                .and(SeanceSpecification.hasType(type));
        var events = seanceRepository.findAll(specification);
        List<SeanceDto> mappedDto = events.stream().
                map(SeanceDto::fromEntity)
                .collect(Collectors.toList());
        return encryptSeance(mappedDto);
    }

    public String getReservedSeats(String uid) throws SeanceNotFoundException, EncryptFailed {
        var optionalSeance = seanceRepository.findByUuid(uid);
        Seance seance = optionalSeance.orElseThrow(() ->
                new SeanceNotFoundException(String.format("Seance with uid %s not found", uid)));
        List<String> reservedSeats = seance.getReservedSeats().stream().toList();
        return encryptSeats(reservedSeats);
    }

    public void reserveSeat(String seanceUid, List<String> reservedSeat) throws SeanceNotFoundException, ReserveSeatsFailedException {
        var optionalSeance = seanceRepository.findByUuid(seanceUid);
        Seance seance = optionalSeance.orElseThrow(() ->
                new SeanceNotFoundException(String.format("Seance with uid %s not found", seanceUid)));
        boolean resultOfReserved = addSeatsToSeance(seance, reservedSeat);
        if(resultOfReserved){
            log.info("Seats successfully reserved!");
            seanceRepository.save(seance);
        }else {
            log.info("Error. Cannot reserve seats");
            throw new ReserveSeatsFailedException("These seats are currently taken");
        }
    }

    private String encryptSeance(List<SeanceDto> data) throws EncryptFailed {
        try {
            String str = mapper.writeValueAsString(data);
            return encrypter.encrypt(str);
        } catch (Exception e) {
            throw new EncryptFailed("Cannot encrypt seance data");
        }
    }

    private String encryptSeats(List<String> data) throws EncryptFailed {
        try {
            String str = mapper.writeValueAsString(data);
            return encrypter.encrypt(str);
        } catch (Exception e) {
            throw new EncryptFailed("Cannot encrypt seats");
        }
    }

    private boolean addSeatsToSeance(Seance seance, List<String> reservedSeat){
        for(String seat: reservedSeat){
            if(! seance.reserveSeat(seat)){
                return false;
            }
        }
        return true;
    }
}

