package com.example.reservation.service.cinema.service;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories("com.example.reservation.service.cinema.domain.repositories")
@EntityScan("com.example.reservation.service.cinema.domain.model")
public class Application {

}
