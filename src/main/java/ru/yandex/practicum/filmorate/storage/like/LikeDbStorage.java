package ru.yandex.practicum.filmorate.storage.like;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;

@Repository
public class LikeDbStorage implements LikeStorage {
    private final JdbcTemplate jdbc;
    private final GenreStorage genreStorage;

    public LikeDbStorage(JdbcTemplate jdbc, GenreStorage genreStorage) {
        this.jdbc = jdbc;
        this.genreStorage = genreStorage;
    }

    @Override
    public void addLike(long filmId, long userId) {
        jdbc.update("MERGE INTO likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    @Override
    public void removeLike(long filmId, long userId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ? AND user_id = ?", filmId, userId);
    }

    @Override
    public int getLikesCount(long filmId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM likes WHERE film_id = ?", Integer.class, filmId);
    }

    @Override
    public List<Film> getTopLikedFilms(int limit) {
        String sql = "SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.rating AS rating, " + "       COUNT(l.user_id) AS like_count " + "  FROM films f " + "  LEFT JOIN likes l         ON f.id = l.film_id " + "  LEFT JOIN mpa_rating m    ON f.mpa    = m.id " + " GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.rating " + " ORDER BY like_count DESC, f.id ASC " + " LIMIT ?";
        List<Film> films = jdbc.query(sql, new FilmRowMapper(), limit);


        for (Film film : films) {
            List<Genre> gs = genreStorage.getGenresByFilmId(film.getId());
            gs.sort(Comparator.comparingInt(Genre::getId));
            film.setGenres(new LinkedHashSet<>(gs));
        }
        return films;
    }

    @Override
    public void removeAllLikesByFilmId(long filmId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ?", filmId);
    }
}
