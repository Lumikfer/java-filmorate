package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.model.ActivityLog;
import ru.yandex.practicum.filmorate.storage.ActivityLogStorage;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;
import java.util.Map.Entry;

import java.util.*;
import java.util.stream.Collectors;

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

    public List<User> addFriend(int userId, int friendId) {
        getUserOrThrow(userId);
        getUserOrThrow(friendId);
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        userStorage.addFriend(userId, friendId);
        activityLogStorage.addActivity(userId, "FRIEND", "ADD", friendId);
        return List.of(userStorage.getUserById(userId), userStorage.getUserById(friendId));
    }

    public User getUserOrThrow(int id) {
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
        getUserOrThrow(id);
        List<User> friends = new ArrayList<>();
        for (User user : userStorage.getFriends(id)) {
            if (user == null) {
                throw new NotFoundException("User not found with id: " + id);
            } else {
                friends.add(user);
            }
        }
        return friends;
    }

    public List<User> getMutualFriends(int id1, int id2) {
        return userStorage.getCommonFriends(id1, id2);
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        userStorage.removeFriend(userId, friendId);
        activityLogStorage.addActivity(userId, "FRIEND", "REMOVE", friendId);
    }

    //рекомендации
    public List<Film> recomendation(int userId) {
        List<Film> allfilm = new ArrayList<>(filmStorage.getFilms());
        List<User> alluser = new ArrayList<>(userStorage.getUsers());
        List<Film> films = new ArrayList<>();
        Map<User, Integer> mapa = new HashMap<>();
        for (User users : alluser) {
            for (Film film : allfilm) {
                if (users.getId() == userId) {
                    continue;
                }
                if (film.getLike().contains(userId) && film.getLike().contains(users.getId())) {
                    films.add(film);
                    if (mapa.containsKey(users)) {
                        log.info("добавлен " + users.getId());
                        mapa.put(users, mapa.get(users) + 1);
                    } else {
                        mapa.put(users, 1);
                    }
                }
            }
        }

        LinkedHashMap<User, Integer> sortedMap =
                mapa.entrySet().stream()
                        .sorted(Entry.comparingByValue())
                        .collect(Collectors.toMap(entry -> entry.getKey(),
                                entry -> entry.getValue(),
                                (e1, e2) -> e1, LinkedHashMap::new));


        User recuser = sortedMap.firstEntry().getKey();
        for (Film film : allfilm) {
            if (film.getLike().contains(recuser.getId()) && !film.getLike().contains(userId)) {
                films.add(film);
            }
        }

        return films;

    }

    public List<ActivityLog> getActivityLogForUserId(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        return activityLogStorage.getFeedForUser(id);
    }

}