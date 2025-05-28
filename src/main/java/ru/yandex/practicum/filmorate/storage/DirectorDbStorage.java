package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Repository
@Slf4j
@RequiredArgsConstructor
public class DirectorDbStorage implements DirectorStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Director createDirector(Director director) {
        log.info("Создание режиссера: {}", director.getName());
        String sql = "INSERT INTO directors (name) VALUES (?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update((PreparedStatementCreator) connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"director_id"});
            ps.setString(1, director.getName());
            return ps;
        }, keyHolder);
        Integer generatedId = Objects.requireNonNull(keyHolder.getKey()).intValue();
        director.setId(generatedId);
        log.info("Режиссер успешно создан с Id: {}", generatedId);
        return director;
    }


    @Override
    public Collection<Director> getAllDirectors() {
        log.info("Получение списка всех режиссеров");
        String sql = "SELECT director_id, name FROM directors ORDER BY director_id ASC";
        return jdbcTemplate.query(sql, this::mapRowToDirector);
    }


    @Override
    public Director getDirectorById(Integer id) {
        log.debug("Получение режиссера с Id: {}", id);
        try {
            String sql = "SELECT director_id, name FROM directors WHERE director_id = ?";
            Director director = jdbcTemplate.queryForObject(sql, this::mapRowToDirector, id);
            log.debug("Режиссер найден: {}", director);
            return director;
        } catch (EmptyResultDataAccessException e) {
            log.error("Режиссер с Id: {}, не найден", id);
            throw new NotFoundException("Режиссер с Id " + id + " не найден.");
        }
    }


    @Override
    public void deleteDirectorById(Integer id) {
        log.info("Удаление режиссера с id: {}", id);
        if (getDirectorById(id) == null) {
            throw new NotFoundException("Режиссер с Id " + id + " не найден.");
        }
        String updateFilmsSql = "DELETE FROM film_directors WHERE director_id = ?";
        jdbcTemplate.update(updateFilmsSql, id);
        String deleteDirectorSql = "DELETE FROM directors WHERE director_id = ?";
        int rowsAffected = jdbcTemplate.update(deleteDirectorSql, id);
        log.info("Режиссер с id {} удален", id);
    }


    @Override
    public Director updateDirector(Director director) {
        log.info("Обновление режиссера с Id: {}", director.getId());
        getDirectorById(director.getId());
        String sql = "UPDATE directors SET name = ? WHERE director_id = ?";
        int rowsAffected = jdbcTemplate.update(sql, director.getName(), director.getId());
        log.info("Режиссер с ID {} успешно обновлен", director.getId());
        return director;
    }


    @Override
    public List<Director> getDirectorsByFilmId(int filmId) {
        log.debug("Получение режиссера для фильма с Id: {}", filmId);

        String sql = "SELECT d.director_id, d.name " +
                "FROM film_directors fd " +
                "JOIN directors d ON fd.director_id = d.director_id " +
                "WHERE fd.film_id = ?";
        try {
            return jdbcTemplate.query(sql, this::mapRowToDirector, filmId);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Нет режиссера для фильма с ID: {}", filmId);
            return Collections.emptyList();
        }
    }


    private Director mapRowToDirector(ResultSet rs, int rowNum) throws SQLException {
        return new Director(rs.getInt("director_id"), rs.getString("name"));
    }

}
