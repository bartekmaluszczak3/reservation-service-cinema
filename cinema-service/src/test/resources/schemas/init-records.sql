-- persistence test
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

-- searching test
insert into movie(id, uuid, title, type) values (4, 'movie-id-4', 'jaws', 'horror');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (4, 'event-id4', now(), 'movie-id-4', 'room-id-1');

insert into movie(id, uuid, title, type) values (5, 'movie-id-5', 'marvel', 'action');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (5, 'event-id5', '2004-10-19 10:23:54', 'movie-id-5', 'room-id-1');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (6, 'event-id6', '2004-11-13 10:23:54', 'movie-id', 'room-id-1');

insert into event(id, uuid, start_time, movie_uid, room_uid) values (7, 'event-id7', '2400-10-19 10:23:54', 'movie-id-2', 'room-id-1');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (8, 'event-id8', '2400-10-19 10:23:54', 'movie-id-2', 'room-id-1');

insert into movie(id, uuid, title, type) values (6, 'movie-id-6', 'babygirl', 'thriller');
insert into movie(id, uuid, title, type) values (7, 'movie-id-7', 'labirint', 'thriller');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (9, 'event-id9', '2030-10-20 10:23:54', 'movie-id-2', 'room-id-1');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (10, 'event-id10', '2030-11-19 10:23:54', 'movie-id-6', 'room-id-1');
insert into event(id, uuid, start_time, movie_uid, room_uid) values (11, 'event-id11', '2030-11-23 10:23:54', 'movie-id-7', 'room-id-1');
