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
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        return activityLogStorage.getFeedForUser(id);
    }

}