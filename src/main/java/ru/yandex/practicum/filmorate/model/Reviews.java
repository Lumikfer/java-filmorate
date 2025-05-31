package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reviews {
    Integer reviewId;

    @NotNull
    String content;

    @NotNull
    Boolean isPositive;

    @NotNull
    Integer userId;

    @NotNull
    Integer filmId;

    int useful;

    public void incrementUseful() {
        this.useful++;
    }

    public void decrementUseful() {
        this.useful--;
    }
}