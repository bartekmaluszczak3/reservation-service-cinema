insert into room(id, uuid, name, capacity) values (1, 'room-id-1', 'room-one', 250);
insert into room(id, uuid, name, capacity) values (2, 'room-id-2', 'room-two', 30);

insert into movie(id, uuid, title, type) values (1, 'movie-id', 'gladiator', 'action');
insert into movie(id, uuid, title, type) values (2, 'movie-id-2', 'seven', 'horror');

insert into event(id, uuid, start_time, movie_uid, room_uid, reserved_seats )
    values (1, 'event-id1', now(), 'movie-id', 'room-id-1', array['1', '12']);

insert into event(id, uuid, start_time, movie_uid, room_uid)
    values (2, 'event-id2', now(), 'movie-id', 'room-id-1');

insert into event(id, uuid, start_time, movie_uid, room_uid, reserved_seats )
    values (3, 'event-id3', now(), 'movie-id-2', 'room-id-2', array['121', '11', '12']);