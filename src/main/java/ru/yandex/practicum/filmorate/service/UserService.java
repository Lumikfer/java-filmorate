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

    public void addFriend(int userId, int friendId) {
        userStorage.getUserById(userId);
        userStorage.getUserById(friendId);
        if (userId == friendId) {
            throw new ValidationException("Нельзя добавить себя в друзья");
        }
        if (userStorage.chekFriendsForUser(userId, friendId)) {
            return;
        }
        activityLogStorage.addActivity(userId, "FRIEND", "ADD", friendId);
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

        if (!userStorage.chekFriendsForUser(userId, friendId)) {
            return;
        }
        activityLogStorage.addActivity(userId, "FRIEND", "REMOVE", friendId);
        userStorage.removeFriend(userId, friendId);
    }

    public List<Film> recommendation(int userId) {
        List<Film> allFilms = new ArrayList<>(filmStorage.getFilms());
        Collection<User> allUsers = userStorage.getUsers();

        Set<Integer> currentUserLikes = allFilms.stream()
                .filter(f -> f.getLike().contains(userId))
                .map(Film::getId)
                .collect(Collectors.toSet());

        Map<Integer, Integer> similarityMap = new HashMap<>();

        for (User user : allUsers) {
            if (user.getId() == userId) continue;

            Set<Integer> userLikes = allFilms.stream()
                    .filter(f -> f.getLike().contains(user.getId()))
                    .map(Film::getId)
                    .collect(Collectors.toSet());

            Set<Integer> commonLikes = new HashSet<>(currentUserLikes);
            commonLikes.retainAll(userLikes);

            similarityMap.put(user.getId(), commonLikes.size());
        }

        Optional<Map.Entry<Integer, Integer>> bestMatch = similarityMap.entrySet()
                .stream()
                .max(Map.Entry.comparingByValue());

        if (bestMatch.isEmpty() || bestMatch.get().getValue() == 0) {
            return Collections.emptyList();
        }

        int recommendedUserId = bestMatch.get().getKey();

        return allFilms.stream()
                .filter(f ->
                        f.getLike().contains(recommendedUserId) &&
                                !f.getLike().contains(userId) &&
                                !currentUserLikes.contains(f.getId())
                )
                .collect(Collectors.toList());
    }

    public List<ActivityLog> getActivityLogForUserId(int id) {
        userStorage.getUserById(id);
        return activityLogStorage.getFeedForUser(id);
    }

}