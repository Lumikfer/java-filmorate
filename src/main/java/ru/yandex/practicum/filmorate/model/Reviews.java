package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reviews {
    Integer reviewId;
    String content;
    Boolean isPositive;
    int userId;
    int filmId;
    int useful;

    public void incrementUseful() {
        this.useful++;
    }

    public void decrementUseful() {
        this.useful--;
    }
}