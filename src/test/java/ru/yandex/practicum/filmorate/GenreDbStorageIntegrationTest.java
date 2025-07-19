package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(GenreDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreDbStorageIntegrationTest {

    private final GenreDbStorage genreStorage;

    @Test
    void getAll_containsSix() {
        List<Genre> all = genreStorage.getAll();
        assertThat(all).hasSize(6)
                .extracting(Genre::getName)
                .contains("Комедия", "Драма", "Мультфильм", "Триллер", "Документальный", "Боевик");
    }

    @Test
    void findById_whenExists() {
        Optional<Genre> g = genreStorage.findById(4);
        assertThat(g).isPresent()
                .get().hasFieldOrPropertyWithValue("name", "Триллер");
    }

    @Test
    void findById_unknown_empty() {
        assertThat(genreStorage.findById(999)).isEmpty();
    }
}
