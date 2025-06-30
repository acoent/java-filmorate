package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;
import java.util.Optional;

public interface FilmStorage {

    Optional<Film> findById(long id);

    List<Film> getFilms();

    Film create(Film film);

    Film update(Film film);

    void delete(long id);
}
