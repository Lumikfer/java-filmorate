package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@RequiredArgsConstructor
@Primary
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public User addUser(User user) {

        String sql = "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"user_id"});
            ps.setString(1, user.getEmail());
            ps.setString(2, user.getLogin());
            ps.setString(3, user.getName());
            ps.setDate(4, Date.valueOf(user.getBirthday()));
            return ps;
        }, keyHolder);

        user.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return user;
    }


    @Override
    public User getUserById(int id) {

        String sql = "SELECT * FROM users WHERE user_id = ?";
        List<User> users = jdbcTemplate.query(sql, this::mapRowToUser, id);

        if (users.isEmpty()) {
            throw new NotFoundException("Такого пользователя нет");
        } else {
            User user = users.get(0);
            List<User> friends = getFriends(user.getId());
            Set<Integer> frinedsId = new HashSet<>();
            for (User user1 : friends) {
                frinedsId.add(user1.getId());
            }
            user.setFriends(frinedsId);
            return user;
        }
    }

    @Override
    public User updateUser(User user) {

        String sql = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbcTemplate.update(sql, user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), user.getId());

        if (rowsUpdated == 0) {
            throw new RuntimeException("User not found with id: " + user.getId());
        }
        return user;
    }

    @Override
    public User deleteUserById(int id) {
        User user = getUserById(id);
        if (user == null) {
            throw new NotFoundException("User not found with id: " + id);
        }
        String sql = "DELETE FROM users WHERE user_id = ?";
        jdbcTemplate.update(sql, id);
        return user;
    }

    @Override
    public void addFriend(int userId, int friendId) {
        if (getUserById(userId) == null || getUserById(friendId) == null) {
            throw new NotFoundException("One or both users not found");
        }

        String sql = "MERGE INTO FRIENDS (USER_ID, FRIEND_ID) VALUES (?, ?)";
        jdbcTemplate.update(sql, userId, friendId);
    }

    public void removeFriend(int userId, int friendId) {
        String sql = "DELETE FROM FRIENDS WHERE USER_ID = ? AND FRIEND_ID = ?";
        jdbcTemplate.update(sql, userId, friendId);
    }

    @Override
    public Collection<User> getUsers() {

        String sql = "SELECT * FROM users";
        return jdbcTemplate.query(sql, this::mapRowToUser);
    }

    private User mapRowToUser(ResultSet rs, int rowNum) throws SQLException {

        return User.builder()
                .id(rs.getInt("user_id"))
                .email(rs.getString("email"))
                .login(rs.getString("login"))
                .name(rs.getString("name"))
                .birthday(rs.getDate("birthday")
                        .toLocalDate()).build();

    }

    @Override
    public List<User> getFriends(int userId) {

        String sql = "SELECT u.* FROM users u " + "JOIN friends f ON u.user_id = f.friend_id " + "WHERE f.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId);
    }

    @Override
    public List<User> getCommonFriends(int userId, int otherId) {

        String sql = "SELECT u.* FROM users u " + "JOIN friends f1 ON u.user_id = f1.friend_id " + "JOIN friends f2 ON u.user_id = f2.friend_id " + "WHERE f1.user_id = ? AND f2.user_id = ?";
        return jdbcTemplate.query(sql, this::mapRowToUser, userId, otherId);
    }
}