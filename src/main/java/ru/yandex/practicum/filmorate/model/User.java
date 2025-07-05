package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
@EqualsAndHashCode(of = {"id"})
public class User {
    protected long id;
    protected String email;
    protected String login;
    protected String name;
    protected LocalDate birthday;
    private Set<Friendship> friendships = new HashSet<>();
}