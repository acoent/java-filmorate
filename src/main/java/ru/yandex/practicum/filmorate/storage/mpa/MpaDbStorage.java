package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;
import java.util.Optional;

@Repository
public class MpaDbStorage implements MpaStorage {

    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<MpaRating> getAll() {
        return jdbcTemplate.query("SELECT id, rating FROM mpa_rating ORDER BY id", new MpaRowMapper());
    }

    @Override
    public Optional<MpaRating> findById(int id) {
        return jdbcTemplate.query("SELECT id, rating FROM mpa_rating WHERE id = ?", new MpaRowMapper(), id).stream().findFirst();
    }
}
