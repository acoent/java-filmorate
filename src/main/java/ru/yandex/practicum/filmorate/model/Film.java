package ru.yandex.practicum.filmorate.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    protected long id;
    protected String name;
    protected String description;
    protected LocalDate releaseDate;
    protected long duration;
    Set<Long> likes = new HashSet<>();
}
