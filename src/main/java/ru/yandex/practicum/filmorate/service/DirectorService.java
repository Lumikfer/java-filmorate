package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.storage.director.DirectorStorage;

import java.util.Collection;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class DirectorService {
    private final DirectorStorage directorStorage;


    public Director createDirector(Director director) {
        log.info("Создание режиссера: {}", director.getName());
        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссера не может быть пустым");
        }
        return directorStorage.createDirector(director);
    }

    public Director updateDirector(Director director) {
        log.info("Обновление режиссера с ID: {}", director.getId());

        if (director.getName() == null || director.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Имя режиссера не может быть пустым");
        }
        getDirectorOrThrow(director.getId());
        return directorStorage.updateDirector(director);
    }

    public Director getDirectorOrThrow(Integer id) {
        Director director = directorStorage.getDirectorById(id);
        if (director == null) {
            throw new NotFoundException("Режиссер с ID " + id + " не найден.");
        }
        return director;
    }

    public Collection<Director> getAllDirectors() {
        return directorStorage.getAllDirectors();
    }

    public void deleteDirectorById(Integer id) {
        directorStorage.deleteDirectorById(id);
        log.info("Режиссер с ID {} успешно удален", id);
    }

    public List<Director> getDirectorsByFilmId(int filmId) {
        return directorStorage.getDirectorsByFilmId(filmId);
    }
}
