package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, MpaDbStorage.class, GenreDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageIntegrationTest {

    private final FilmDbStorage filmStorage;
    private final MpaDbStorage mpaStorage;
    private final GenreDbStorage genreStorage;

    @Test
    void getFilms_initialEmpty() {
        List<Film> all = filmStorage.getFilms();
        assertThat(all).isEmpty();
    }

    @Test
    void createAndFindById() {
        Film f = new Film();
        f.setName("A");
        f.setDescription("B");
        f.setReleaseDate(LocalDate.of(2000, 1, 1));
        f.setDuration(100);
        f.setMpa(mpaStorage.findById(1).get());
        f.setGenres(Set.of(genreStorage.findById(2).get()));

        Film created = filmStorage.create(f);
        assertThat(created.getId()).isPositive();

        Optional<Film> fetched = filmStorage.findById(created.getId());
        assertThat(fetched).isPresent()
                .get().satisfies(f2 -> {
                    assertThat(f2.getMpa().getId()).isEqualTo(1);
                    assertThat(f2.getGenres()).extracting(Genre::getId).containsExactly(2);
                });
    }

    @Test
    void update_changesDataAndGenres() {
        Film f = new Film();
        f.setName("X");
        f.setDescription("Y");
        f.setReleaseDate(LocalDate.now());
        f.setDuration(50);
        f.setMpa(mpaStorage.findById(2).get());
        f.setGenres(Set.of(genreStorage.findById(3).get()));
        Film c = filmStorage.create(f);

        c.setName("X2");
        c.setGenres(Set.of(genreStorage.findById(4).get()));
        Film updated = filmStorage.update(c);
        assertThat(updated.getName()).isEqualTo("X2");
        assertThat(updated.getGenres()).extracting(Genre::getId).containsExactly(4);
    }

    @Test
    void delete_removesFilm() {
        Film f = new Film();
        f.setName("T");
        f.setDescription("D");
        f.setReleaseDate(LocalDate.now());
        f.setDuration(30);
        f.setMpa(mpaStorage.findById(1).get());
        f.setGenres(Set.of());
        Film c = filmStorage.create(f);
        filmStorage.delete(c.getId());
        assertThat(filmStorage.findById(c.getId())).isEmpty();
    }
}
