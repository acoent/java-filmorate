package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.friendship.FriendshipStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {
    private final UserStorage userStorage;
    private final FriendshipStorage friendshipStorage;

    public UserService(@Qualifier("userDbStorage") UserStorage userStorage, @Qualifier("friendshipDbStorage") FriendshipStorage friendshipStorage) {
        this.userStorage = userStorage;
        this.friendshipStorage = friendshipStorage;
    }

    private void validateUser(User user) {
        log.debug("Validating user: {}", user);
        if (user.getEmail() == null || user.getEmail().isBlank() || !user.getEmail().contains("@")) {
            log.warn("Invalid email: {}", user.getEmail());
            throw new ValidationException("Email must not be empty and must contain '@'.");
        }
        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            log.warn("Invalid login: {}", user.getLogin());
            throw new ValidationException("Login must not be empty or contain spaces.");
        }
        if (user.getBirthday() != null && user.getBirthday().isAfter(LocalDate.now())) {
            log.warn("Birthday in future: {}", user.getBirthday());
            throw new ValidationException("Birthday cannot be in the future.");
        }
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Name is blank, setting login as name: {}", user.getLogin());
            user.setName(user.getLogin());
        }
    }

    public User create(User user) {
        log.info("Creating user: {}", user);
        validateUser(user);
        User created = userStorage.create(user);
        log.info("Created user: {}", created);
        return created;
    }

    public User update(User user) {
        log.info("Updating user: {}", user);
        validateUser(user);
        User updated = userStorage.update(user);
        log.info("Updated user: {}", updated);
        return updated;
    }

    private void ensureExists(long id) {
        if (userStorage.findById(id).isEmpty()) {
            log.warn("User not found: id={}", id);
            throw new NotFoundException("User with id=" + id + " not found.");
        }
    }

    public void addFriend(long userId, long friendId) {
        log.info("User {} adding friend {}", userId, friendId);
        if (userId == friendId) {
            log.warn("Attempt to add oneself as friend: {}", userId);
            throw new ValidationException("Cannot add yourself as friend.");
        }
        ensureExists(userId);
        ensureExists(friendId);
        friendshipStorage.sendFriendRequest(userId, friendId);
        log.info("User {} successfully sent friend request to {}", userId, friendId);
    }

    public void removeFriend(long userId, long friendId) {
        log.info("User {} removing friend {}", userId, friendId);
        ensureExists(userId);
        ensureExists(friendId);
        friendshipStorage.removeFriend(userId, friendId);
        log.info("User {} removed friend {}", userId, friendId);
    }

    public List<User> getFriends(long userId) {
        log.info("Fetching friends for user {}", userId);
        ensureExists(userId);
        List<Long> ids = friendshipStorage.getFriends(userId);
        List<User> friends = ids.stream().map(userStorage::findById).flatMap(Optional::stream).collect(Collectors.toList());
        log.info("User {} has friends: {}", userId, ids);
        return friends;
    }

    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("Fetching common friends between {} and {}", userId, otherId);
        ensureExists(userId);
        ensureExists(otherId);

        Set<Long> set1 = new HashSet<>(friendshipStorage.getFriends(userId));
        Set<Long> set2 = new HashSet<>(friendshipStorage.getFriends(otherId));
        set1.retainAll(set2);

        List<User> common = set1.stream().map(userStorage::findById).flatMap(Optional::stream).collect(Collectors.toList());
        log.info("Common friends of {} and {}: {}", userId, otherId, set1);
        return common;
    }

    public List<User> getUsers() {
        log.info("Fetching all users");
        List<User> all = userStorage.getUsers();
        log.info("Found {} users", all.size());
        return all;
    }

    public Optional<User> findById(long id) {
        log.info("Looking up user id={}", id);
        return userStorage.findById(id);
    }

    public void delete(long id) {
        log.info("Deleting user id={}", id);
        userStorage.delete(id);
        log.info("Deleted user id={}", id);
    }
}
