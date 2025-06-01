package com.example.reservation.service.cinema.service.exception;

public class ReserveSeatsFailedException extends Exception{
    public ReserveSeatsFailedException(String errorMessage) {
        super(errorMessage);
    }

}
