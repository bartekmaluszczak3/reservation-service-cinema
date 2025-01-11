package com.example.reservation.service.cinema.domain.repositories;

import com.example.reservation.service.cinema.domain.model.Event;
import com.example.reservation.service.cinema.domain.model.Movie;
import jakarta.persistence.criteria.Join;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;

public class EventSpecification {

    public static Specification<Event> before(LocalDateTime beforeDate){
        return ((root, query, criteriaBuilder) ->
                beforeDate != null ? criteriaBuilder.lessThanOrEqualTo(root.get("startTime"), beforeDate) : criteriaBuilder.conjunction());

    }

    public static Specification<Event> after(LocalDateTime afterDate){
        return ((root, query, criteriaBuilder) ->
                afterDate != null ? criteriaBuilder.greaterThanOrEqualTo(root.get("startTime"), afterDate) : criteriaBuilder.conjunction());
    }

    public static Specification<Event> hasType(String type){
        return ((root, query, criteriaBuilder) -> {
            if(type.isBlank()){
                return criteriaBuilder.conjunction();
            }
            Join<Event, Movie> movieJoin = root.join("movie");
            return criteriaBuilder.equal(movieJoin.get("type"), type);
        });
    }
}
