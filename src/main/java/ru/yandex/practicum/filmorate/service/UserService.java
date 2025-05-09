package ru.yandex.practicum.filmorate.service;

import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
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
        User userFirst = userStorage.getUserById(idf);
        User userSecond = userStorage.getUserById(ids);
        userFirst.friendsadd(userSecond);
        userSecond.friendsadd(userFirst);
        log.info("В друзья добавились " + userFirst.getName() + " и " + userSecond.getName());
        List<User> friend = new ArrayList<>();
        friend.add(userFirst);
        friend.add(userSecond);
        return friend;
    }

    public User addUser(User user) {
        if (userStorage.getUsers().contains(user)) {
            throw new ValidationException("---");
        }
        userStorage.addUser(user);
        return user;
    }

    public User updateUser(User user) {
        userStorage.updateUser(user);
        return user;
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

    public void deleteFriend(int userid, int friendid) {
        User userf = userStorage.getUserById(userid);
        User users = userStorage.getUserById(friendid);
        userf.deleteFriend(friendid);
        users.deleteFriend(userid);
        log.info(userStorage.getUserById(userid)+ " и "+userStorage.getUserById(friendid)+" удалились из друзей");
    }


}
