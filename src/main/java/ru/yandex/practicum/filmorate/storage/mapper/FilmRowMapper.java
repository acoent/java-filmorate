package ru.yandex.practicum.filmorate.storage.mapper;

import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.MpaRating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;

public class FilmRowMapper implements RowMapper<Film> {
    @Override
    public Film mapRow(ResultSet rs, int rowNum) throws SQLException {
        long id = rs.getLong("id");
        String name = rs.getString("name");
        String description = rs.getString("description");
        LocalDate rd = rs.getDate("release_date").toLocalDate();
        long duration = rs.getLong("duration");
        Integer mpaId = rs.getObject("mpa", Integer.class);
        String mpaRating = rs.getString("rating");

        Film film = new Film();
        film.setId(id);
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(rd);
        film.setDuration(duration);

        if (mpaId != null) {
            film.setMpa(new MpaRating(mpaId, mpaRating));
        }
        return film;
    }
}
