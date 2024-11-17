package com.example.reservation.service.cinema.service.persistance;

import com.example.reservation.service.cinema.domain.repositories.EventRepository;
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
    EventRepository eventRepository;


    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
    }

    @AfterEach
    void afterEach() throws IOException {
        container.clearRecords();
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearDatabase();
    }

    @BeforeEach
    void beforeEach() throws IOException {
        container.initRecords();
    }

    @Test
    @SneakyThrows
    void checkDatabaseStructure(){
        // when
        var rooms = roomRepository.findAll();
        var movies = movieRepository.findAll();
        var events = eventRepository.findAll();

        // then
        Assertions.assertEquals(2, rooms.size());
        Assertions.assertEquals(2, movies.size());
        Assertions.assertEquals(3, events.size());

        // and
        var eventWithReservedSeats = eventRepository.findById(3L);
        Assertions.assertEquals(3, eventWithReservedSeats.get().getReservedSeats().size());
    }

    @Test
    void shouldReserveSeat(){
        // given
        var event = eventRepository.findById(3L).get();
        var seatToAdd = "1234";

        // when
        event.reserveSeat(seatToAdd);
        eventRepository.saveAndFlush(event);

        // then
        var persistedEvent = eventRepository.findById(3L).get();
        Assertions.assertEquals(4, persistedEvent.getReservedSeats().size());
        Assertions.assertTrue(persistedEvent.getReservedSeats().contains(seatToAdd));
    }

    @Test
    void shouldCancelSeat(){
        // given
        var event = eventRepository.findById(3L).get();
        var seatToRemove = "11";

        // when
        event.reserveSeat(seatToRemove);
        eventRepository.saveAndFlush(event);

        // then
        var persistedEvent = eventRepository.findById(3L).get();
        Assertions.assertEquals(2, persistedEvent.getReservedSeats().size());
        Assertions.assertFalse(persistedEvent.getReservedSeats().contains(seatToRemove));
    }
}
