package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAllGenres() {

        String sql = "SELECT * FROM genres";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(Integer id) {
        log.debug("Fetching genre with id: {}", id);
        try {
            String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ?";
            Genre genre = jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
            log.debug("Found genre: {}", genre);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.error("Genre not found. ID: {}", id);
            throw new NotFoundException("Genre not found with id: " + id);
        }
    }

    @Override
    public Set<Genre> getFilmGenres(int filmId) {

        String sql = "SELECT g.* FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ?";
        return new HashSet<>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {

        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }
}