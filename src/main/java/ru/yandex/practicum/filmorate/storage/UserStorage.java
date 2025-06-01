package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {

    User addUser(User user);

    User deleteUserById(int id);

    User getUserById(int id);

    User updateUser(User user);

    Collection<User> getUsers();

    void removeFriend(int id1, int id2);

    void addFriend(int userId, int friendId);

    List<User> getFriends(int userId);

    List<User> getCommonFriends(int userId, int otherId);

    Boolean chekFriendsForUser(int userId, int friendId);
}
