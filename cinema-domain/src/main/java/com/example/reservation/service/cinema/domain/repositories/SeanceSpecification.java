package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Seance;
import com.example.reservation.service.cinema.domain.model.Movie;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class SeanceSpecification {

    public static Specification<Seance> before(LocalDateTime beforeDate){
        return ((root, query, criteriaBuilder) ->
                beforeDate != null ? criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), beforeDate) : criteriaBuilder.conjunction());

    }

    public static Specification<Seance> after(LocalDateTime afterDate){
        return ((root, query, criteriaBuilder) ->
                afterDate != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), afterDate) : criteriaBuilder.conjunction());
    }

    public static Specification<Seance> hasType(String type){
        return ((root, query, criteriaBuilder) -> {
            if(type.isBlank()){
                return criteriaBuilder.conjunction();
            }
            Join<Seance, Movie> movieJoin = root.join("movie");
            return criteriaBuilder.equal(movieJoin.get("type"), type);
        });
    }
}
