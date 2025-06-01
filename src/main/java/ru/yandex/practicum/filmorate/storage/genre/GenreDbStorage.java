package ru.yandex.practicum.filmorate.storage.genre;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Repository
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage implements GenreStorage {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Genre> getAllGenres() {
        String sql = "SELECT * FROM genres ORDER BY genre_id ASC";
        return jdbcTemplate.query(sql, this::mapRowToGenre);
    }

    @Override
    public Genre getGenreById(Integer id) {
        log.debug("Fetching genre with id: {}", id);
        try {
            String sql = "SELECT genre_id, name FROM genres WHERE genre_id = ? ";
            Genre genre = jdbcTemplate.queryForObject(sql, this::mapRowToGenre, id);
            log.debug("Found genre: {}", genre);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.error("Genre not found. ID: {}", id);
            throw new NotFoundException("Genre not found with id: " + id);
        }
    }

    @Override
    public List<Genre> getFilmGenres(int filmId) {
        String sql = "SELECT g.* FROM film_genres fg " +
                "JOIN genres g ON fg.genre_id = g.genre_id " +
                "WHERE fg.film_id = ? " +
                "ORDER BY g.genre_id ASC";
        return new ArrayList<Genre>(jdbcTemplate.query(sql, this::mapRowToGenre, filmId));
    }

    private Genre mapRowToGenre(ResultSet rs, int rowNum) throws SQLException {

        return new Genre(rs.getInt("genre_id"), rs.getString("name"));
    }

    public void insertInFilmGenreTable(Film film) {
        if (film.getGenres() == null) {
            return;
        }

        Set<Integer> genreIds = film.getGenres().stream()
                .map(Genre::getId)
                .collect(Collectors.toSet());

        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());


           List<Object[]> batch = genreIds.stream()
                .map(genreId -> new Object[]{film.getId(), genreId})
                .toList();
        jdbcTemplate.batchUpdate("INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)", batch);
    }

}