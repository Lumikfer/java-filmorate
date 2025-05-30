package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reviews {
    Integer reviewId;


    @NotBlank(message = "Content cannot be blank")
    private String content;

    @NotNull(message = "isPositive cannot be null")
    private Boolean isPositive;

    @NotNull(message = "User ID cannot be null")
    private Integer userId;

    @NotNull(message = "Film ID cannot be null")
    private Integer filmId;

    int useful;


    public void incrementUseful() {
        this.useful++;
    }

    public void decrementUseful() {
        this.useful--;
    }
}