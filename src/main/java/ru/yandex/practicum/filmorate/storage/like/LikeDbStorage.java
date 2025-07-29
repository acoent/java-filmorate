package ru.yandex.practicum.filmorate.storage.like;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreStorage;
import ru.yandex.practicum.filmorate.storage.mapper.FilmRowMapper;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        String sql = """
                SELECT f.id, f.name, f.description, f.release_date, f.duration, f.mpa,
                       m.rating AS rating, COUNT(l.user_id) AS like_count
                FROM films f
                LEFT JOIN likes l ON f.id = l.film_id
                LEFT JOIN mpa_rating m ON f.mpa = m.id
                GROUP BY f.id, f.name, f.description, f.release_date, f.duration, f.mpa, m.rating
                ORDER BY like_count DESC, f.id ASC
                LIMIT ?
                """;
        List<Film> films = jdbc.query(sql, new FilmRowMapper(), limit);
        if (films.isEmpty()) return films;
        String inSql = films.stream().map(f -> String.valueOf(f.getId())).collect(Collectors.joining(","));

        String genreSql = """
                SELECT fg.film_id, g.id, g.name
                FROM film_genres fg
                JOIN genres g ON fg.genre_id = g.id
                WHERE fg.film_id IN (%s)
                """.formatted(inSql);
        List<Map<String, Object>> rows = jdbc.queryForList(genreSql);
        Map<Long, LinkedHashSet<Genre>> genresByFilm = new HashMap<>();
        for (Map<String, Object> row : rows) {
            Long filmId = ((Number) row.get("film_id")).longValue();
            Integer genreId = ((Number) row.get("id")).intValue();
            String name = (String) row.get("name");
            Genre genre = new Genre();
            genre.setId(genreId);
            genre.setName(name);
            genresByFilm.computeIfAbsent(filmId, k -> new LinkedHashSet<>()).add(genre);
        }
        for (Film film : films) {
            LinkedHashSet<Genre> genres = genresByFilm.getOrDefault(film.getId(), new LinkedHashSet<>());
            film.setGenres(genres);
        }

        return films;
    }

    @Override
    public void removeAllLikesByFilmId(long filmId) {
        jdbc.update("DELETE FROM likes WHERE film_id = ?", filmId);
    }
}
