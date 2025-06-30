package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    private void validateUser(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            throw new ValidationException("Электронная почта не может быть пустой и должна содержать символ '@'.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Логин не может быть пустым и не должен содержать пробелы.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
    }

    public User create(User user) {
        validateUser(user);
        return userStorage.create(user);
    }

    public User update(User user) {
        validateUser(user);
        return userStorage.update(user);
    }

    public User getUserOrThrow(long id) {
        return userStorage.findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден."));
    }

    public void addFriend(long userId, long friendId) {
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья.");
        }
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);
        user.getFriends().add(friendId);
        friend.getFriends().add(userId);
    }

    public void removeFriend(long userId, long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }

    public List<User> getFriends(long userId) {
        return getUserOrThrow(userId).getFriends().stream().map(userStorage::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }


    public List<User> getCommonFriends(long userId, long otherId) {
        User user = getUserOrThrow(userId);
        User other = getUserOrThrow(otherId);

        Set<Long> commonIds = user.getFriends().stream().filter(other.getFriends()::contains).collect(Collectors.toSet());

        return commonIds.stream().map(userStorage::findById).filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());
    }
}