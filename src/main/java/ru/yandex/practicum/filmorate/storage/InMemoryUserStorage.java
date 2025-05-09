package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Component
public class InMemoryUserStorage implements UserStorage {


    Map<Integer, User> users = new HashMap<>();
    private static int id = 1;

    @Override
    public User addUser(User user) {
        user.setId(id++);
        users.put(user.getId(), user);
        return user;

    }

    @Override
    public User deleteUserById(int id) {
        User user = users.get(id);
        if (user == null) {
            throw new RuntimeException("Пользователь не найден");
        }
        users.remove(user);
        return user;
    }

    @Override
    public User getUserById(int id) {
        if (users.get(id) == null) {
            throw new RuntimeException("Пользователь не найден");
        }
        return users.get(id);
    }

    @Override
    public User updateUser(User user) {
        if (user == null || !users.containsValue(user)) {
            throw new RuntimeException("Пользователь не найден");
        }
        users.put(user.getId(), user);
        return user;

    }

    @Override
    public Collection<User> getUsers() {
        return users.values();
    }
}
