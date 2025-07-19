package ru.yandex.practicum.filmorate.storage.user;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<User> getUsers() {
        return jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users", new UserRowMapper());
    }

    @Override
    public Optional<User> findById(long id) {
        return jdbcTemplate.query("SELECT id, email, login, name, birthday FROM users WHERE id = ?", new UserRowMapper(), id).stream().findFirst();
    }

    @Override
    public User create(User user) {
        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(conn -> {
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            if (user.getBirthday() != null) {
                ps.setDate(4, Date.valueOf(user.getBirthday()));
            } else {
                ps.setDate(4, null);
            }
            return ps;
        }, keyHolder);
        Number id = keyHolder.getKey();
        if (id == null) throw new NotFoundException("Не удалось получить ID пользователя.");
        user.setId(id.longValue());
        return user;
    }

    @Override
    public User update(User user) {
        int rows = jdbcTemplate.update("UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?", user.getEmail(), user.getLogin(), user.getName(), Date.valueOf(user.getBirthday()), user.getId());
        if (rows == 0) throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
        return user;
    }

    @Override
    public void delete(long id) {
        jdbcTemplate.update("DELETE FROM users WHERE id = ?", id);
    }
}