package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.ActivityLog;
import ru.yandex.practicum.filmorate.storage.activityLog.ActivityLogStorage;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;
    private final FilmStorage filmStorage;

    private final ActivityLogStorage activityLogStorage;


    public User addUser(User user) {

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        return userStorage.addUser(user);
    }

    public void addFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        activityLogStorage.addActivity(userId, "FRIEND", "ADD", friendId);
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        if (userStorage.chekFriendsForUser(userId, friendId)) {
            return;
        }
        userStorage.addFriend(userId, friendId);
    }

    public User getUserById(int id) {
        return userStorage.getUserById(id);
    }

    public User updateUser(User user) {
        userStorage.getUserById(user.getId());
        return userStorage.updateUser(user);
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public void deleteUserById(int id) {
        userStorage.deleteUserById(id);
    }

    public List<User> getFriendByIdUser(int userId) {
        userStorage.getUserById(userId);
        return userStorage.getFriends(userId);
    }

    public List<User> getCommonFriends(int id1, int id2) {
        userStorage.getUserById(id1);
        userStorage.getUserById(id2);
        return userStorage.getCommonFriends(id1, id2);
    }

    public void deleteFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        activityLogStorage.addActivity(userId, "FRIEND", "REMOVE", friendId);

        if (!userStorage.chekFriendsForUser(userId, friendId)) {
            return;
        }
        userStorage.removeFriend(userId, friendId);
    }

    public List<Film> recommendation(int userId) {
        return filmStorage.getRecomendationFilm(userId);
    }

    public List<ActivityLog> getActivityLogForUserId(int id) {
        userStorage.getUserById(id);
        return activityLogStorage.getFeedForUser(id);
    }

}