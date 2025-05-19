package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    @Qualifier("UserDbStorage")
    private final UserStorage userStorage;


    public User addUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public List<User> addFriend(int userId, int friendId) {
        getUserOrThrow(userId); // Проверяет существование
        getUserOrThrow(friendId);

        userStorage.addFriend(userId, friendId);
        return List.of(userStorage.getUserById(userId), userStorage.getUserById(friendId));
    }

    private User getUserOrThrow(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        return user;
    }

    public User updateUser(User user) {

        getUserOrThrow(user.getId());
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.updateUser(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public List<User> approveFriend(int userId, int friendId) {

        if (userStorage.getUserById(userId).getFriends().contains(friendId)) {
            addFriend(userId, friendId);
            userStorage.updateUser(userStorage.getUserById(userId));
            userStorage.updateUser(userStorage.getUserById(friendId));
            return List.of(userStorage.getUserById(userId), userStorage.getUserById(friendId));
        }
        return List.of(userStorage.getUserById(userId));
    }

    public void deleteUserById(int id) {
        userStorage.deleteUserById(id);
    }

    public List<User> getFriendByIdUser(int id) {
        return userStorage.getFriends(id);
    }

    public List<User> getMutualFriends(int id1, int id2) {
        return userStorage.getCommonFriends(id1, id2);
    }

    public void deleteFriend(int userId, int friendId) {
        userStorage.removeFriend(userId, friendId);
    }


}