package com.example.reservation.service.cinema.service.crypto.encrypter;

public class NoSecureEncrypter implements Encrypter{
    @Override
    public String encrypt(String data) {
        return data;
    }
}
