package ru.yandex.practicum.filmorate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.model.User;

import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {
    private UserController controller;
    private User validUser;

    @BeforeEach
    void setUp() {
        controller = new UserController();
        validUser = new User();
        validUser.setLogin("testUser");
        validUser.setEmail("test@mail.ru");
    }

    @Test
    void updateUser_ExistingUser_ShouldUpdateData() {
        User added = controller.adduser(validUser);
        added.setName("New Name");

        User updated = controller.updateUser(added);

        assertEquals("New Name", updated.getName());
    }

    @Test
    void getUsers_ShouldReturnAllUsers() {
        controller.adduser(validUser);
        List<User> users = controller.getUsers();

        assertEquals(1, users.size());
        assertEquals("testUser", users.get(0).getLogin());
    }
}