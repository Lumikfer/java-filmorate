package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.*;

@Data
public class Film {

    private int id;

    @NotBlank(message = "Name cannot be blank")
    private String name;

    @Size(max = 200, message = "Description cannot be longer than 200 characters")
    private String description;

    private LocalDate releaseDate;

    @Positive(message = "Duration must be positive")
    private int duration;

    private List<Genre> genres = new ArrayList<>();

    private Mpa mpa;

    private List<Director> directors = new ArrayList<>();

    private Double rating;

}