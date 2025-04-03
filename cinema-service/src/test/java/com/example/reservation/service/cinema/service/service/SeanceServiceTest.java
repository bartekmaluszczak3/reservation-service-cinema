package com.example.reservation.service.cinema.service.service;

import com.example.reservation.service.cinema.service.Application;
import com.example.reservation.service.cinema.service.exception.EncryptFailed;
import com.example.reservation.service.cinema.service.exception.ReserveSeatsFailedException;
import com.example.reservation.service.cinema.service.exception.SeanceNotFoundException;
import com.example.reservation.service.cinema.service.utils.PostgresContainer;
import lombok.SneakyThrows;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.util.List;
import java.util.Map;


@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT, classes = Application.class)
@ContextConfiguration(initializers = PostgresContainer.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class SeanceServiceTest {

    @Autowired
    SeanceService seanceService;


    private static final PostgresContainer container = new PostgresContainer();

    @BeforeAll
    void beforeAll() throws IOException {
        container.initDatabase();
        container.initRecords();
    }

    @AfterEach
    void afterEach(){
        container.execute("delete from reservation;");
    }

    @AfterAll
    void afterAll() throws IOException {
        container.clearRecords();
        container.clearDatabase();
    }

    @SneakyThrows
    @Test
    void shouldReserveSeatsWhenTheyAreFree(){
        // given
        String seanceUid = "seance-id5";
        String userUid = "dummy";
        List<String> seats = List.of("1", "2");

        // when
        seanceService.reserveSeat(seanceUid, userUid, seats);

        var reservation = getReservationForUser(userUid);
        Assertions.assertEquals(1, reservation.size());
        var persistentSeanceUid = reservation.get(0).get("seance_uid").toString();
        Assertions.assertEquals(seanceUid, persistentSeanceUid);

        String reservedSeat = reservation.get(0).get("reserved_seats").toString();
        seats.forEach(e-> Assertions.assertTrue(reservedSeat.contains(e)));
    }
    
    @Test
    void shouldNotReserveSeatsIfAreTaken() throws EncryptFailed, SeanceNotFoundException {
        // given
        String seanceUid = "seance-id3";
        String userUid = "dummy";
        List<String> seats = List.of("1", "12", "123");

        // when and then
        Assertions.assertThrows(ReserveSeatsFailedException.class,
                ()-> seanceService.reserveSeat(seanceUid, userUid, seats));

        String reservedSeats = seanceService.getReservedSeats("seance-id3");
        Assertions.assertFalse(reservedSeats.contains("12"));
        Assertions.assertFalse(reservedSeats.contains("123"));
    }

    @SneakyThrows
    @Test
    void shouldNotReserveSeatsWhenSeanceDoesNotExist(){
        // given
        String seanceUid = "dummy";
        String userUid = "dummy";
        List<String> seats = List.of("1", "12", "123");

        // when and then
        Assertions.assertThrows(SeanceNotFoundException.class,
                ()-> seanceService.reserveSeat(seanceUid, userUid, seats));

        var reservations = getReservationForUser(userUid);
        Assertions.assertEquals(0, reservations.size());
    }


    private List<Map<String, Object>> getReservationForUser(String userUid){
        return container.executeQueryForObjects("SELECT * from reservation where user_uid ='" + userUid + "';");
    }
}
