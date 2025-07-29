package ru.yandex.practicum.filmorate.storage.genre;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mapper.GenreRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    public GenreDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Genre> getAll() {
        return jdbcTemplate.query("SELECT id, name FROM genres ORDER BY id", new GenreRowMapper());
    }

    @Override
    public Optional<Genre> findById(int id) {
        return jdbcTemplate.query("SELECT id, name FROM genres WHERE id = ?", new GenreRowMapper(), id).stream().findFirst();
    }

    @Override
    public List<Genre> getGenresByFilmId(long filmId) {
        String sql = "SELECT g.id, g.name FROM genres g " + "JOIN film_genres fg ON g.id = fg.genre_id " + "WHERE fg.film_id = ?";
        return jdbcTemplate.query(sql, new GenreRowMapper(), filmId);
    }


}
