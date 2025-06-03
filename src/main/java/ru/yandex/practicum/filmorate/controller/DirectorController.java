package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.List;

@RestController
@RequestMapping("/directors")
@RequiredArgsConstructor
public class DirectorController {
    private final DirectorService directorService;


    @PostMapping
    public Director addDirector(@RequestBody Director director) {
        return directorService.createDirector(director);
    }

    @PutMapping
    public Director updateDirector(@RequestBody Director director) {
        return directorService.updateDirector(director);
    }

    @GetMapping("/{id}")
    public Director getDirectorById(@PathVariable Integer id) {
        return directorService.getDirectorOrThrow(id);
    }

    @GetMapping
    public List<Director> getAllDirectors() {
        return (List<Director>) directorService.getAllDirectors();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDirector(@PathVariable Integer id) {
        directorService.deleteDirectorById(id);
    }

    @GetMapping("/{filmId}/films")
    public List<Director> getDirectorsByFilmId(@PathVariable int filmId) {
        return directorService.getDirectorsByFilmId(filmId);
    }
}