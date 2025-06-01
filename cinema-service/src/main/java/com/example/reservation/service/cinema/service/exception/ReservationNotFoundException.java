package com.example.reservation.service.cinema.service.exception;

public class ReservationNotFoundException extends Exception{
    public ReservationNotFoundException(String errorMessage) {
        super(errorMessage);
    }

}
