package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    @Autowired
    UserService userService;

    @PostMapping
    public User adduser(@Valid @RequestBody User user) {
        userService.addUser(user);
        return user;
    }

    @PutMapping
    public ResponseEntity<User> updateUser(@Valid @RequestBody User user) {
        User updatedUser = userService.updateUser(user);
        if (updatedUser == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updatedUser);
    }

    @GetMapping
    public Collection<User> getUsers() {
        return userService.getUsers();
    }

    @PutMapping("/{userId}/friends/{friendId}")
    public void addUser(@PathVariable int userId, @PathVariable int friendId) {
        userService.addFriend(userId, friendId);
    }

    @GetMapping("/{id}/friends")
    public List<User> getUser(@PathVariable int id) {
        return userService.getFriends(id);
    }

    @GetMapping("/{userid}/friends/common/{otherId}")
    public List<User> getMutualFriend(@PathVariable int userid, @PathVariable int otherId) {
        return userService.getCommonFriends(userid, otherId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userid, @PathVariable int friendId) {
        userService.deleteFriends(userid, friendId);
    }


}
