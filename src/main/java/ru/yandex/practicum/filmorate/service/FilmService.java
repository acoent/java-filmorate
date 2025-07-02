package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }


    public List<Film> getFilms() {
        return filmStorage.getFilms();
    }

    public void deleteFilm(long id) {
        Film film = getFilmOrThrow(id);
        film.getLikes().clear();
        filmStorage.delete(id);
    }

    public Film create(Film film) {
        validateFilm(film);
        Film created = filmStorage.create(film);
        log.info("Фильм создан: {}", created);
        return created;
    }

    public Film update(Film film) {
        validateFilm(film);
        Film updated = filmStorage.update(film);
        log.info("Фильм обновлён: {}", updated);
        return updated;
    }

    public Film getFilmOrThrow(long id) {
        return filmStorage.findById(id).orElseThrow(() -> new NotFoundException("Фильм с id = " + id + " не найден."));
    }

    public void addLike(long filmId, long userId) {
        validateUserExists(userId);
        Film film = getFilmOrThrow(filmId);
        film.getLikes().add(userId);
        filmStorage.update(film);
        log.info("Пользователь {} поставил лайк фильму {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        validateUserExists(userId);
        Film film = getFilmOrThrow(filmId);
        film.getLikes().remove(userId);
        filmStorage.update(film);
        log.info("Пользователь {} убрал лайк с фильма {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        return sortByLikesDesc(filmStorage.getFilms()).stream().limit(count).collect(Collectors.toList());
    }

    private List<Film> sortByLikesDesc(List<Film> films) {
        return films.stream().sorted(Comparator.comparingInt((Film f) -> f.getLikes().size()).reversed()).collect(Collectors.toList());
    }

    private void validateFilm(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Название не может быть пустым.");
        }
        if (film.getDescription().length() > 200) {
            throw new ValidationException("Максимальная длина описания — 200 символов.");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года.");
        }
        if (film.getDuration() < 1) {
            throw new ValidationException("Продолжительность фильма должна быть положительным числом.");
        }
    }

    private void validateUserExists(long userId) {
        userStorage.findById(userId).orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

}
