package ru.yandex.practicum.filmorate;

import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.model.Film;

import java.io.IOException;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {
    private FilmController controller;
    private Film validFilm;

    @BeforeEach
    void setUp() {
        controller = new FilmController();
        validFilm = new Film();
        validFilm.setName("Inception");
        validFilm.setReleaseDate(LocalDate.of(2010, 7, 16));
        validFilm.setDuration(148);
    }

    @Test
    void addFilm_ValidFilm_ShouldAddToStorage() throws IOException {
        Film result = controller.addFilm(validFilm);

        assertNotNull(result.getId());
        assertEquals(1, controller.getFilms().size());
    }

    @Test
    void addFilm_InvalidDate_ShouldThrowException() {
        Film invalidFilm = new Film();
        invalidFilm.setReleaseDate(LocalDate.of(1890, 1, 1));

        assertThrows(ValidationException.class, () ->
                controller.addFilm(invalidFilm)
        );
    }

    @Test
    void updateFilm_ExistingId_ShouldUpdateFilm() {
        Film addedFilm = controller.addFilm(validFilm);
        addedFilm.setName("Updated Name");

        Film updated = controller.updateFilm(addedFilm);

        assertEquals("Updated Name", updated.getName());
    }
}