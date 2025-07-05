package ru.yandex.practicum.filmorate.service;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Friendship;
import ru.yandex.practicum.filmorate.model.FriendshipStatus;
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

        boolean friendRequested = friend.getFriendships().stream()
                .anyMatch(f -> f.getFriendId() == userId);

        user.getFriendships().removeIf(f -> f.getFriendId() == friendId);
        user.getFriendships().add(new Friendship(friendId,
                friendRequested ? FriendshipStatus.CONFIRMED : FriendshipStatus.PENDING));

        friend.getFriendships().removeIf(f -> f.getFriendId() == userId);
        friend.getFriendships().add(new Friendship(userId,
                friendRequested ? FriendshipStatus.CONFIRMED : FriendshipStatus.PENDING));
    }


    public void removeFriend(long userId, long friendId) {
        User user = getUserOrThrow(userId);
        User friend = getUserOrThrow(friendId);

        user.getFriendships().removeIf(f -> f.getFriendId() == friendId);
        friend.getFriendships().removeIf(f -> f.getFriendId() == userId);
    }


    public List<User> getFriends(long userId) {
        return getUserOrThrow(userId).getFriendships().stream()
                .filter(f -> f.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }



    public List<User> getCommonFriends(long userId, long otherId) {
        Set<Long> userFriends = getUserOrThrow(userId).getFriendships().stream()
                .filter(f -> f.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        Set<Long> otherFriends = getUserOrThrow(otherId).getFriendships().stream()
                .filter(f -> f.getStatus() == FriendshipStatus.CONFIRMED)
                .map(Friendship::getFriendId)
                .collect(Collectors.toSet());

        userFriends.retainAll(otherFriends);

        return userFriends.stream()
                .map(userStorage::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }


    public List<User> getUsers() {
        return userStorage.getUsers();
    }

    public Optional<User> findById(long id) {
        return userStorage.findById(id);
    }

    public void delete(long id) {
        userStorage.delete(id);
    }

}