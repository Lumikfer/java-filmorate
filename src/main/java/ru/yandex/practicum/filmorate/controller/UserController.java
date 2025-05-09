package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
    public User updateUser(@Valid @RequestBody User user) {

        userService.updateUser(user);
        return user;
    }

    @GetMapping
    public Collection<User> getUsers() {
        return  userService.getUsers();
    }

    @PutMapping("{userId}/{userId2}/friend")
    public void addUser(@PathVariable int userId,@PathVariable int userId2) {
        userService.addFriend(userId,userId2);
    }

    @GetMapping("{id}/friends")
    public List<User> getUser(@PathVariable int id) {
        return userService.getFriendByIdUser(id);
    }

    @GetMapping("{userid}/friends/common/{otherId}")
    public List<User> getMutualFriend(@PathVariable int userid,@PathVariable int otherId) {
        return userService.getMutualFriends(userid,otherId);
    }

    @DeleteMapping("{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int userid,@PathVariable int friendId) {
        userService.deleteFriend(userid,friendId);
    }


}
