package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import(MpaDbStorage.class)
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class MpaDbStorageIntegrationTest {

    private final MpaDbStorage mpaStorage;

    @Test
    void getAll_containsAllRatings() {
        List<MpaRating> all = mpaStorage.getAll();
        assertThat(all).hasSize(5).extracting(MpaRating::getName).containsExactly("G", "PG", "PG-13", "R", "NC-17");
    }

    @Test
    void findById_existing_returnsRating() {
        Optional<MpaRating> m = mpaStorage.findById(3);
        assertThat(m).isPresent().get().hasFieldOrPropertyWithValue("name", "PG-13");
    }

    @Test
    void findById_unknown_returnsEmpty() {
        assertThat(mpaStorage.findById(999)).isEmpty();
    }
}
