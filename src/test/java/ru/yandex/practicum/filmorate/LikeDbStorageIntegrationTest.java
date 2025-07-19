package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.film.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.genre.GenreDbStorage;
import ru.yandex.practicum.filmorate.storage.like.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({LikeDbStorage.class, FilmDbStorage.class, UserDbStorage.class, GenreDbStorage.class, MpaDbStorage.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class LikeDbStorageIntegrationTest {

    private final LikeDbStorage likeStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    @Test
    void addRemoveAndCountLikes() {
        User u = userStorage.create(new User(0, "u@u", "u", "U", LocalDate.now()));
        Film f = new Film();
        f.setName("L");
        f.setDescription("D");
        f.setReleaseDate(LocalDate.now());
        f.setDuration(10);
        f.setMpa(mpaStorage.findById(1).get());
        f.setGenres(Set.of());
        Film cf = filmStorage.create(f);

        likeStorage.addLike(cf.getId(), u.getId());
        assertThat(likeStorage.getLikesCount(cf.getId())).isEqualTo(1);

        likeStorage.removeLike(cf.getId(), u.getId());
        assertThat(likeStorage.getLikesCount(cf.getId())).isZero();
    }

    @Test
    void getTopLikedFilms_ordersByLikes() {
        User u1 = userStorage.create(new User(0, "u1@", "u1", "U1", LocalDate.now()));
        User u2 = userStorage.create(new User(0, "u2@", "u2", "U2", LocalDate.now()));

        Film f1 = new Film();
        f1.setName("A");
        f1.setDescription("D");
        f1.setReleaseDate(LocalDate.now());
        f1.setDuration(5);
        f1.setMpa(mpaStorage.findById(1).get());
        f1.setGenres(Set.of());
        Film c1 = filmStorage.create(f1);

        Film f2 = new Film();
        f2.setName("B");
        f2.setDescription("D");
        f2.setReleaseDate(LocalDate.now());
        f2.setDuration(5);
        f2.setMpa(mpaStorage.findById(1).get());
        f2.setGenres(Set.of());
        Film c2 = filmStorage.create(f2);

        likeStorage.addLike(c2.getId(), u1.getId());
        likeStorage.addLike(c2.getId(), u2.getId());
        likeStorage.addLike(c1.getId(), u1.getId());

        List<Film> top = likeStorage.getTopLikedFilms(2);
        assertThat(top).extracting(Film::getId).containsExactly(c2.getId(), c1.getId());
    }
}
