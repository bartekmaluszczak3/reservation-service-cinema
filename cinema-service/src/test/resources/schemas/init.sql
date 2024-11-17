create table room (
    id bigint PRIMARY KEY,
    name VARCHAR(255) UNIQUE NOT NULL,
    capacity integer NOT NULL
);

create sequence room_seq increment 1 start 1;

create table movie (
    id bigint PRIMARY KEY,
    title VARCHAR(255) unique NOT NULL,
    type VARCHAR(255)
);

create sequence movie_seq increment 1 start 1;


create table event(
   id bigint PRIMARY KEY,
   start_time timestamp without time zone,
   movie_id bigint,
       CONSTRAINT fk_movie
        FOREIGN KEY(movie_id)
            REFERENCES movie(id),
   room_id bigint,
        CONSTRAINT fk_room
         FOREIGN KEY(room_id)
            REFERENCES room(id),
   reserved_seats text[]
);


