package com.example.reservation.service.cinema.service.exception;

public class EncryptFailed extends Exception{
    public EncryptFailed(String errorMessage) {
        super(errorMessage);
    }
}
