package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController userController;

    @BeforeEach
    void setup() {
        userController = new UserController();
    }

    @Test
    void createUserWithEmptyEmail_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail(" ");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ '@'.", ex.getMessage());
    }

    @Test
    void createUserWithEmailWithoutAt_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("userexample.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Электронная почта не может быть пустой и должна содержать символ '@'.", ex.getMessage());
    }

    @Test
    void createUserWithEmptyLogin_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin(" ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Логин не может быть пустым и не должен содержать пробелы.", ex.getMessage());
    }

    @Test
    void createUserWithLoginContainingSpaces_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("invalid login");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Логин не может быть пустым и не должен содержать пробелы.", ex.getMessage());
    }

    @Test
    void createUserWithFutureBirthday_ShouldThrowValidationException() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setBirthday(LocalDate.now().plusDays(1)); // будущее

        ValidationException ex = assertThrows(ValidationException.class, () -> userController.create(user));
        assertEquals("Дата рождения не может быть в будущем.", ex.getMessage());
    }

    @Test
    void createUserWithEmptyName_ShouldSetNameToLogin() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName(" ");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertEquals("validLogin", created.getName());
    }

    @Test
    void createUserWithValidData_ShouldCreateSuccessfully() {
        User user = new User();
        user.setEmail("user@example.com");
        user.setLogin("validLogin");
        user.setName("UserName");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User created = userController.create(user);

        assertNotNull(created);
        assertEquals(1, created.getId());
        assertEquals("user@example.com", created.getEmail());
    }
}