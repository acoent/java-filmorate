package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
@Data
@EqualsAndHashCode(of = {"id"})
public class User {

    protected long id;
    protected String email;
    protected String login;
    protected String name;
    protected LocalDate birthday;

    public User() {
    }

    public User(long id, String email, String login, String name, LocalDate birthday) {
        this.id = id;
        this.email = email;
        this.login = login;
        this.name = name;
        this.birthday = birthday;
    }
}
