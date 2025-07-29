package ru.yandex.practicum.filmorate.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.MpaRating;
import ru.yandex.practicum.filmorate.storage.mpa.MpaStorage;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {
    @Autowired
    private MpaStorage mpaStorage;

    @GetMapping
    public List<MpaRating> getAll() {
        return mpaStorage.getAll();
    }

    @GetMapping("/{id}")
    public MpaRating getById(@PathVariable int id) {
        return mpaStorage.findById(id).orElseThrow(() -> new NotFoundException("MPA с id=" + id + " не найден"));
    }
}
