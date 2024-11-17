insert into room(id, name, capacity) values (1, 'room-one', 250);
insert into room(id, name, capacity) values (2, 'room-two', 30);

insert into movie(id, title, type) values (1, 'gladiator', 'action');
insert into movie(id, title, type) values (2, 'seven', 'horror');

insert into event(id, start_time, movie_id, room_id, reserved_seats )
    values (1, now(), 1, 1, array['1', '12']);

insert into event(id, start_time, movie_id, room_id)
    values (2, now(), 1, 1);

insert into event(id, start_time, movie_id, room_id, reserved_seats )
    values (3, now(), 2, 2, array['121', '11', '12']);