package com.example.reservation.service.cinema.service.persistance;

import com.example.reservation.service.cinema.domain.model.Reservation;
import com.example.reservation.service.cinema.domain.repositories.ReservationRepository;
import com.example.reservation.service.cinema.domain.repositories.SeanceRepository;
import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class PersistenceTest {

    @Autowired
    ReservationRepository reservationRepository;

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
        var seance = seanceRepository.findById(2L).get();
        var reservation = Reservation.builder()
                .reservationDate(LocalDateTime.now())
                .id(1L)
                .seance(seance)
                .userUuid("userUid")
                .uuid("UUUID")
                .reservedSeats(List.of("1", "2"))
                .build();
        reservationRepository.save(reservation);
        // when

        var reservedSeat = seance.getReservationList();
        Assertions.assertEquals(1, reservedSeat.size());
    }
}
