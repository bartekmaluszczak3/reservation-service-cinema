package com.example.reservation.service.cinema.service.persistance;

import com.example.reservation.service.cinema.domain.repositories.SeanceRepository;
import com.example.reservation.service.cinema.domain.repositories.MovieRepository;
import com.example.reservation.service.cinema.domain.repositories.RoomRepository;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersistenceTest {

    @Autowired
    RoomRepository roomRepository;

    @Autowired
    MovieRepository movieRepository;

    @Autowired
    SeanceRepository seanceRepository;


    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
        container.initRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
    }

    @Test
    @SneakyThrows
    void checkDatabaseStructure(){
        // when
        var eventWithReservedSeats = seanceRepository.findById(3L);

        // then
        Assertions.assertEquals(4, eventWithReservedSeats.get().getReservedSeats().size());
    }

    @Test
    void shouldReserveSeat(){
        // given
        var event = seanceRepository.findById(3L).get();
        var seatToAdd = "1234";

        // when
        event.reserveSeat(seatToAdd);
        seanceRepository.saveAndFlush(event);

        // then
        var persistedEvent = seanceRepository.findById(3L).get();
        Assertions.assertEquals(4, persistedEvent.getReservedSeats().size());
        Assertions.assertTrue(persistedEvent.getReservedSeats().contains(seatToAdd));
    }

    @Test
    void shouldCancelSeat(){
        // given
        var event = seanceRepository.findById(1L).get();
        var seatToRemove = "1";

        // when
        event.cancelSeat(seatToRemove);
        seanceRepository.saveAndFlush(event);

        // then
        var persistedEvent = seanceRepository.findById(1L).get();
        Assertions.assertEquals(1, persistedEvent.getReservedSeats().size());
        Assertions.assertFalse(persistedEvent.getReservedSeats().contains(seatToRemove));
    }
}
