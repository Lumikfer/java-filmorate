package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import org.springframework.web.server.ResponseStatusException;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Slf4j
@Service
public class UserService {
    @Autowired
    private UserStorage userStorage;

    public List<User> addFriend(int idf, int ids) {
        User userFirst = getUserOrThrow(idf);
        User userSecond = getUserOrThrow(ids);

        userFirst.friendsadd(ids);
        userSecond.friendsadd(idf);

        return List.of(userFirst, userSecond);
    }

    private User getUserOrThrow(int id) {
        User user = userStorage.getUserById(id);
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return user;
    }

    public User addUser(User user) {
        if (userStorage.getUsers().contains(user)) {
            throw new ValidationException("---");
        }
        userStorage.addUser(user);
        return user;
    }

    public User updateUser(User user) {
        User existingUser = userStorage.getUserById(user.getId());
        if (existingUser == null) {
            throw new ValidationException("User not found");
        }
        existingUser.setEmail(user.getEmail());
        existingUser.setLogin(user.getLogin());
        existingUser.setName(user.getName());
        existingUser.setBirthday(user.getBirthday());
        return existingUser;
    }

    public Collection<User> getUsers() {
        return userStorage.getUsers();
    }

    public List<User> getFriendByIdUser(int id) {
        List<User> friends = new ArrayList<>();
        for (int idi : userStorage.getUserById(id).getFriends()) {
            if (userStorage.getUserById(idi) == null) {
                throw new RuntimeException("юзера нет");
            }
            friends.add(userStorage.getUserById(idi));
        }
        log.info("друзья юзера " + userStorage.getUserById(id).getName() + " " + friends);
        return friends;

    }

    public List<User> getMutualFriends(int idf, int ids) {
        List<User> mutualFriends = new ArrayList<>();
        for (int i : userStorage.getUserById(idf).getFriends()) {
            for (int j : userStorage.getUserById(ids).getFriends()) {
                if (j == i) {
                    log.info("Найден общий друг " + userStorage.getUserById(i));
                    mutualFriends.add(userStorage.getUserById(i));

                }
            }
        }
        log.info("список общих друзей юзера " + userStorage.getUserById(idf) + " и " + userStorage.getUserById(ids) + ": " + mutualFriends);

        return mutualFriends;
    }

    public void deleteFriend(int userId, int friendId) {
        User user = userStorage.getUserById(userId);
        User friend = userStorage.getUserById(friendId);

        if (user == null || friend == null) {
            throw new ValidationException("User not found");
        }

        user.getFriends().remove(friendId);
        friend.getFriends().remove(userId);
    }


}
