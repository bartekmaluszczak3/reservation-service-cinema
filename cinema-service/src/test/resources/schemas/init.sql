create table room (
    id bigint PRIMARY KEY,
    uuid VARCHAR(255) unique not null,
    name VARCHAR(255) UNIQUE NOT NULL,
    capacity integer NOT NULL
);

create sequence room_seq increment 1 start 1;

create table movie (
    id bigint PRIMARY KEY,
    uuid VARCHAR(255) unique not null,
    title VARCHAR(255) unique NOT NULL,
    type VARCHAR(255)
);

create sequence movie_seq increment 1 start 1;


create table event(
   id bigint PRIMARY KEY,
   uuid VARCHAR(255) unique not null,
   start_time timestamp without time zone,
   movie_uid VARCHAR(255),
       CONSTRAINT fk_movie
        FOREIGN KEY("movie_uid")
            REFERENCES movie("uuid"),
   room_uid VARCHAR(255),
        CONSTRAINT fk_room
         FOREIGN KEY("room_uid")
            REFERENCES room("uuid"),
   reserved_seats text[]
);


