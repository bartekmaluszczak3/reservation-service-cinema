-- persistence test
insert into room(id, uuid, name, capacity) values (1, 'room-id-1', 'room-one', 250);
insert into room(id, uuid, name, capacity) values (2, 'room-id-2', 'room-two', 30);

insert into movie(id, uuid, title, type) values (1, 'movie-id', 'gladiator', 'action');
insert into movie(id, uuid, title, type) values (2, 'movie-id-2', 'seven', 'horror');

insert into seance(id, uuid, start_time, movie_uid, room_uid )
    values (1, 'seance-id1', now(), 'movie-id', 'room-id-1');

insert into seance(id, uuid, start_time, movie_uid, room_uid)
    values (2, 'seance-id2', now(), 'movie-id', 'room-id-1');

insert into seance(id, uuid, start_time, movie_uid, room_uid)
    values (3, 'seance-id3', now(), 'movie-id-2', 'room-id-2');

insert into reservation(id, uuid, user_uid, created_date, modified_date, seance_uid, reserved_seats, status)
    values(91, 'reservation-id-1', 'user-id', '2020-10-20 10:23:54', '2020-10-20 10:23:54', 'seance-id3', array['1', '2'], 'ACTIVE');

insert into reservation(id, uuid, user_uid, created_date, modified_date, seance_uid, reserved_seats, status)
    values(92, 'reservation-id-2', 'user-id', now(), now(), 'seance-id3', array['3', '4'], 'ACTIVE');

-- searching test
insert into movie(id, uuid, title, type) values (4, 'movie-id-4', 'jaws', 'horror');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (4, 'seance-id4', now(), 'movie-id-4', 'room-id-1');

insert into movie(id, uuid, title, type) values (5, 'movie-id-5', 'marvel', 'action');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (5, 'seance-id5', '2004-10-19 10:23:54', 'movie-id-5', 'room-id-1');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (6, 'seance-id6', '2004-11-13 10:23:54', 'movie-id', 'room-id-1');

insert into seance(id, uuid, start_time, movie_uid, room_uid) values (7, 'seance-id7', '2400-10-19 10:23:54', 'movie-id-2', 'room-id-1');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (8, 'seance-id8', '2400-10-19 10:23:54', 'movie-id-2', 'room-id-1');

insert into movie(id, uuid, title, type) values (6, 'movie-id-6', 'babygirl', 'thriller');
insert into movie(id, uuid, title, type) values (7, 'movie-id-7', 'labirint', 'thriller');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (9, 'seance-id9', '2030-10-20 10:23:54', 'movie-id-2', 'room-id-1');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (10, 'seance-id10', '2030-11-19 10:23:54', 'movie-id-6', 'room-id-1');
insert into seance(id, uuid, start_time, movie_uid, room_uid) values (11, 'seance-id11', '2030-11-23 10:23:54', 'movie-id-7', 'room-id-1');
