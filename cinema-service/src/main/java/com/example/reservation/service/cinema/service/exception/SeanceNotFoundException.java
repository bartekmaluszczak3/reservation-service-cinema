package com.example.reservation.service.cinema.service.exception;

public class SeanceNotFoundException extends Exception{
    public SeanceNotFoundException(String errorMessage) {
        super(errorMessage);
    }

}
