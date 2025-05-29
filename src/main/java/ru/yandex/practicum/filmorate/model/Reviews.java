package ru.yandex.practicum.filmorate.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Reviews {

    Integer id;
    String content;
    Boolean isPositive;
    int userId;
    int filmId;
    Integer useFul;

    public Integer plusUseFul(Integer useFul) {
            useFul++;

        return useFul;

    }

    public Integer minusUseFul( Integer useFul) {
            useFul--;

        return useFul;
    }


}
