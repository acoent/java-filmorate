package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(of = {"id"})
public class Film {
    private long id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(max = 200, message = "Description must be less than 200 characters")
    private String description;

    @NotNull(message = "Release date cannot be null")
    @PastOrPresent(message = "Release date cannot be in the future")
    private LocalDate releaseDate;

    @Positive(message = "Duration must be positive")
    private long duration;

    @NotNull(message = "MPA cannot be null")
    private MpaRating mpa;

    private Set<Genre> genres;
}
