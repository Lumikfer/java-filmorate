package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private int id;
    private Set<Integer> friends = new HashSet<>();

    public void friendsadd(User user) {
        if(user.getId() == this.id) {
            throw new IllegalArgumentException("Нельзя добавить себя в друзья");
        }
        friends.add(user.getId());
    }


    private void valid(User user) {
        if (user.getName() == null) {
            user.setName(user.getLogin());
        }
    }


    @NotBlank(message = "Логин не может быть пустым")
    @Pattern(regexp = "\\S+", message = "Логин не может содержать пробелы")
    private String login;

    private String name;

    @NotBlank(message = "Электронная почта не может быть пустой")
    @Email(message = "Электронная почта должна содержать символ @")
    private String email;

    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @JsonFormat(pattern = "yyyy.MM.dd")
    private LocalDate birthday;
}