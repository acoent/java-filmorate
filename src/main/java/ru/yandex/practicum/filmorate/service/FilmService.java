package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.List;

@Slf4j
@Service
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final MpaStorage mpaStorage;
    private final GenreStorage genreStorage;
    private final LikeStorage likeStorage;

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage, @Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("mpaDbStorage") MpaStorage mpaStorage, @Qualifier("genreDbStorage") GenreStorage genreStorage, @Qualifier("likeDbStorage") LikeStorage likeStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.mpaStorage = mpaStorage;
        this.genreStorage = genreStorage;
        this.likeStorage = likeStorage;
    }

    public List<Film> getFilms() {
        log.info("Fetching all films");
        List<Film> films = filmStorage.getFilms();
        log.info("Found {} films", films.size());
        return films;
    }

    public Film create(Film film) {
        log.info("Creating film: {}", film);
        validateMpaAndGenres(film);
        Film created = filmStorage.create(film);
        log.info("Created film with id={}", created.getId());
        return created;
    }

    public Film update(Film film) {
        log.info("Updating film: {}", film);
        validateMpaAndGenres(film);
        Film updated = filmStorage.update(film);
        log.info("Updated film id={}", updated.getId());
        return updated;
    }

    public void deleteFilm(long id) {
        log.info("Deleting film id={}", id);
        getFilmOrThrow(id);
        likeStorage.removeAllLikesByFilmId(id);
        filmStorage.delete(id);
        log.info("Deleted film id={} (and its likes)", id);
    }

    public Film getFilmOrThrow(long id) {
        log.info("Looking up film id={}", id);
        return filmStorage.findById(id).orElseThrow(() -> {
            log.warn("Film not found id={}", id);
            return new NotFoundException("Film with id=" + id + " not found.");
        });
    }

    public void addLike(long filmId, long userId) {
        log.info("User {} likes film {}", userId, filmId);
        validateUserExists(userId);
        getFilmOrThrow(filmId);
        likeStorage.addLike(filmId, userId);
        log.info("User {} liked film {}", userId, filmId);
    }

    public void removeLike(long filmId, long userId) {
        log.info("User {} unlikes film {}", userId, filmId);
        validateUserExists(userId);
        getFilmOrThrow(filmId);
        likeStorage.removeLike(filmId, userId);
        log.info("User {} removed like from film {}", userId, filmId);
    }

    public List<Film> getPopularFilms(int count) {
        log.info("Fetching top {} popular films", count);
        List<Film> popular = likeStorage.getTopLikedFilms(count);
        log.info("Found {} popular films", popular.size());
        return popular;
    }

    private void validateMpaAndGenres(Film film) {
        mpaStorage.findById(film.getMpa().getId()).orElseThrow(() -> new NotFoundException("MPA with id=" + film.getMpa().getId() + " not found."));
        if (film.getGenres() != null) {
            for (Genre g : film.getGenres()) {
                genreStorage.findById(g.getId()).orElseThrow(() -> new NotFoundException("Genre with id=" + g.getId() + " not found."));
            }
        }
    }

    private void validateUserExists(long userId) {
        if (userStorage.findById(userId).isEmpty()) {
            log.warn("User not found id={}", userId);
            throw new NotFoundException("User with id=" + userId + " not found.");
        }
    }
}
