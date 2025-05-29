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

    public Integer sUseFul(Boolean isPositive, Integer useFul) {
        if (isPositive == true) {
            useFul++;
        }
        return useFul;
    }

    public Integer dUseFul(Boolean isPositive, Integer useFul) {
        if (isPositive == false) {
            useFul--;
        }
        return useFul;
    }


}
