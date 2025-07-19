package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(UserDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageIntegrationTest {

    private final UserDbStorage userStorage;

    @Test
    void getUsers_returnsInitialUsers() {
        List<User> all = userStorage.getUsers();
        assertThat(all).isNotEmpty();
    }

    @Test
    void findById_existingUser_returnsUser() {
        Optional<User> u = userStorage.findById(1);
        assertThat(u).isPresent().hasValueSatisfying(user -> assertThat(user.getId()).isEqualTo(1));
    }

    @Test
    void create_update_delete_cycle() {
        User user = new User(0, "a@b.c", "login", "Name", LocalDate.of(1990, 1, 1));
        User created = userStorage.create(user);
        assertThat(created.getId()).isPositive();

        created.setName("NewName");
        userStorage.update(created);
        Optional<User> fetched = userStorage.findById(created.getId());
        assertThat(fetched).isPresent().get().hasFieldOrPropertyWithValue("name", "NewName");

        userStorage.delete(created.getId());
        assertThat(userStorage.findById(created.getId())).isEmpty();
    }
}
