DELETE
FROM mpa_rating;
DELETE
FROM genres;

-- справочники MPA и жанров
INSERT INTO mpa_rating (id, rating)
VALUES (1, 'G'),
       (2, 'PG'),
       (3, 'PG-13'),
       (4, 'R'),
       (5, 'NC-17');

INSERT INTO genres (id, name)
VALUES (1, 'Комедия'),
       (2, 'Драма'),
       (3, 'Мультфильм'),
       (4, 'Триллер'),
       (5, 'Документальный'),
       (6, 'Боевик');

INSERT INTO users (email, login, name, birthday)
VALUES ('u1@example.com', 'user1', 'User One', '1990-01-01');
