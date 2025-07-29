package ru.yandex.practicum.filmorate.storage.friendship;

import java.util.List;

public interface FriendshipStorage {
    void sendFriendRequest(long userId, long friendId);

    void removeFriend(long userId, long friendId);

    List<Long> getFriends(long userId);
}