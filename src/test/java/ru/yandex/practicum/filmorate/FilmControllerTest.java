package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController filmController;

    @BeforeEach
    void setup() {
        filmController = new FilmController();
    }

    @Test
    void createFilmWithEmptyName_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("   ");  // пустое название
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Название не может быть пустым.", ex.getMessage());
    }

    @Test
    void createFilmWithTooLongDescription_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("a".repeat(201)); // длина > 200
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Максимальная длина описания — 200 символов.", ex.getMessage());
    }

    @Test
    void createFilmWithEarlyReleaseDate_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(1800, 1, 1)); // раньше 28.12.1895
        film.setDuration(90);

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Дата релиза — не раньше 28 декабря 1895 года.", ex.getMessage());
    }

    @Test
    void createFilmWithZeroDuration_ShouldThrowValidationException() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0); // 0 — недопустимо

        ValidationException ex = assertThrows(ValidationException.class, () -> filmController.create(film));
        assertEquals("Продолжительность фильма должна быть положительным числом.", ex.getMessage());
    }

    @Test
    void createFilmWithValidData_ShouldCreateSuccessfully() {
        Film film = new Film();
        film.setName("Фильм");
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        Film createdFilm = filmController.create(film);

        assertNotNull(createdFilm);
        assertEquals(1, createdFilm.getId());
        assertEquals("Фильм", createdFilm.getName());
    }
}