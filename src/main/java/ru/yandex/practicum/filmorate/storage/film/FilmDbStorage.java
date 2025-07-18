package ru.yandex.practicum.filmorate.storage.film;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;

@Repository
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;

    public FilmDbStorage(JdbcTemplate jdbcTemplate, GenreStorage genreStorage) {
        this.jdbcTemplate = jdbcTemplate;
        this.genreStorage = genreStorage;
    }

    @Override
    public List<Film> getFilms() {
        String sql = """
                SELECT f.id,
                       f.name,
                       f.description,
                       f.release_date,
                       f.duration,
                       f.mpa,
                       m.rating AS rating
                  FROM films f
                  LEFT JOIN mpa_rating m ON f.mpa = m.id
                """;
        List<Film> films = jdbcTemplate.query(sql, new FilmRowMapper());
        for (Film film : films) {
            List<Genre> gs = genreStorage.getGenresByFilmId(film.getId());
            gs.sort(Comparator.comparingInt(Genre::getId));
            film.setGenres(new LinkedHashSet<>(gs));
        }
        return films;
    }

    @Override
    public Optional<Film> findById(long id) {
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa,
                       m.rating AS rating
                  FROM films f
                  LEFT JOIN mpa_rating m ON f.mpa = m.id
                 WHERE f.id = ?
                """;
        Optional<Film> maybe = jdbcTemplate.query(sql, new FilmRowMapper(), id).stream().findFirst();

        maybe.ifPresent(film -> {
            List<Genre> gs = genreStorage.getGenresByFilmId(film.getId());
            gs.sort(Comparator.comparingInt(Genre::getId));
            film.setGenres(new LinkedHashSet<>(gs));
        });

        return maybe;
    }

    @Override
    public Film create(Film film) {
        String sql = "INSERT INTO films (name, description, release_date, duration, mpa) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setLong(4, film.getDuration());
            ps.setLong(5, film.getMpa().getId());
            return ps;
        }, keyHolder);
        long newId = Objects.requireNonNull(keyHolder.getKey()).longValue();
        film.setId(newId);
        saveGenres(film);
        return findById(newId).get();
    }

    @Override
    public Film update(Film film) {
        String sql = "UPDATE films SET name=?, description=?, release_date=?, duration=?, mpa=? WHERE id=?";
        int rows = jdbcTemplate.update(sql, film.getName(), film.getDescription(), Date.valueOf(film.getReleaseDate()), film.getDuration(), film.getMpa().getId(), film.getId());

        if (rows == 0) {
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
        }
        saveGenres(film);
        return findById(film.getId()).get();
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", id);
        jdbcTemplate.update("DELETE FROM likes        WHERE film_id=?", id);
        jdbcTemplate.update("DELETE FROM films        WHERE id=?", id);
    }

    private void saveGenres(Film film) {
        long filmId = film.getId();
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id=?", filmId);
        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            for (Genre g : film.getGenres()) {
                jdbcTemplate.update("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", filmId, g.getId());
            }
        }
    }
}
