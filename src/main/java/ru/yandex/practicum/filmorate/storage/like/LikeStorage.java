package ru.yandex.practicum.filmorate.storage.like;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.List;

public interface LikeStorage {
    void addLike(long filmId, long userId);

    void removeLike(long filmId, long userId);

    int getLikesCount(long filmId);

    List<Film> getTopLikedFilms(int limit);

    void removeAllLikesByFilmId(long filmId);

}
