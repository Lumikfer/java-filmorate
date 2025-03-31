package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import jakarta.validation.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    Map<Integer, User> users = new HashMap<>();
    private static int userId = 1;

    @PostMapping
    public User adduser(@Valid @RequestBody User user) {
        user.setId(userId++);
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
        users.put(user.getId(), user);
        log.info("Пользователь добавлен id: " + user.getId());
        return user;
    }

    @PutMapping
    public User updateUser(@Valid @RequestBody User user) {
        if (!users.containsKey(user.getId())) {
            throw new ValidationException("Такого пользователя нет");
        }
        users.put(user.getId(), user);
        log.info("пользователь обновлен");
        return user;
    }

    @GetMapping
    public List<User> getUsers() {
        List<User> user = new ArrayList<>(users.values());
        return user;
    }
}
