package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {
    private final JdbcTemplate jdbcTemplate;
    private static final int ZERO = 0;


    @Override
    public Film addFilm(Film film) {

        String sql = "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";

        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, new String[]{"film_id"});
            ps.setString(1, film.getName());
            ps.setString(2, film.getDescription());
            ps.setDate(3, Date.valueOf(film.getReleaseDate()));
            ps.setInt(4, film.getDuration());
            ps.setInt(5, film.getMpa().getId());
            return ps;
        }, keyHolder);

        film.setId(Objects.requireNonNull(keyHolder.getKey()).intValue());
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        String sql = "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, " +
                "mpa_id = ? WHERE film_id = ?";
        jdbcTemplate.update(
                sql,
                film.getName(),
                film.getDescription(),
                Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Collection<Film> getFilms() {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id";
        return jdbcTemplate.query(sql, this::mapRowToFilm);
    }

    @Override
    public Film getFilmById(int id) {
        String sql = "SELECT f.*, m.name AS mpa_name " +
                "FROM films f " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id = ?";
        List<Film> films = jdbcTemplate.query(sql, this::mapRowToFilm, id);
        if (films.isEmpty()) {
            throw new NotFoundException("Такого фильма нет");
        }
        return films.getFirst();
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(getMpa(rs.getInt("mpa_id")));
        film.setGenres(getGenres(rs.getInt("film_id")));
        film.setRating(getRatings(rs.getInt("film_id")));
        film.setDirectors(getDirectors(rs.getInt("film_id")));
        return film;
    }

    private Mpa getMpa(int mpaId) {
        String sql = "SELECT * FROM mpa WHERE mpa_id = ?";
        return jdbcTemplate.queryForObject(sql, (rs, rowNum)
                -> new Mpa(rs.getInt("mpa_id"), rs.getString("name")), mpaId);
    }

    private List<Genre> getGenres(int filmId) {
        String sql = """
                SELECT g.genre_id, g.name
                FROM genres g
                JOIN film_genres fg ON g.genre_id = fg.genre_id
                WHERE fg.film_id = ?
                ORDER BY g.genre_id
                """;
        List<Genre> genres = jdbcTemplate.query(sql, ((rs, rowNum) ->
                new Genre(rs.getInt("genre_id"), rs.getString("name"))), filmId);
        genres.sort(Comparator.comparingInt(Genre::getId));
        return genres;
    }

    private List<Director> getDirectors(int filmId) {
        String sql = """
            SELECT d.director_id, d.name
            FROM directors d
            JOIN film_directors fd ON d.director_id = fd.director_id
            WHERE fd.film_id = ?
            ORDER BY d.director_id ASC
            """;
        List<Director> directors = jdbcTemplate.query(sql, (rs, rowNum) ->
                new Director(rs.getInt("director_id"), rs.getString("name")), filmId);
        return directors;
    }

    private Double getRatings(int filmId) {
        String sql = "SELECT AVG(rating) AS average_rating FROM film_likes WHERE film_id = ?";
        Double averageRating = jdbcTemplate.queryForObject(sql, new Object[]{filmId}, Double.class);

        return averageRating != null ? averageRating : ZERO;
    }

    @Override
    public void deleteFilmById(int id) {
        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addRating(int filmId, int userId, int rating) {
        String sql = "INSERT INTO film_likes (film_id, user_id, rating) VALUES (?, ?, ?)";
        jdbcTemplate.update(sql, filmId, userId, rating);
    }

    @Override
    public Boolean chekLikeForFilm(int filmId, int userId) {
        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);

        return count != null && count > ZERO;
    }

    @Override
    public void removeLike(int filmId, int userId) {
        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        jdbcTemplate.update(sql, filmId, userId);
    }

    public List<Integer> getLikesForFilm(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return jdbcTemplate.queryForList(sql, Integer.class, filmId);
    }

    @Override
    public List<Film> getPopularFilms(int count, Integer year, Integer genreId) {
        String newsql = "";
        List<Object> params = new ArrayList<>();

        if (year != null) {
            newsql = "WHERE EXTRACT(YEAR FROM f.release_date) = ? ";
            params.add(year);
        }
        if (genreId != null) {
            newsql = "WHERE fg.genre_id = ? ";
            params.add(genreId);
        }
        if (year != null && genreId != null) {
            newsql = "WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ? ";
            params.clear();
            params.add(genreId);
            params.add(year);
        }

        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name" +
                " AS mpa_name " +
                "FROM films f " +
                "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
                "LEFT JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                newsql +
                "GROUP BY f.film_id " +
                "ORDER BY AVG(fl.rating) DESC " +
                "LIMIT ?";

        params.add(count);

        return jdbcTemplate.query(sql, this::mapRowToFilm, params.toArray());
    }


    @Override
    public List<Film> searchFilm(String string) {

        return List.of();
    }

    @Override
    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        String orderByClause = switch (sortBy.toLowerCase()) {
            case "year" -> "f.release_date";
            case "likes" -> "like_count DESC";
            default -> throw new IllegalArgumentException("Неподдерживаемый параметр сортировки: " + sortBy);
        };

        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, " +
                "f.mpa_id, m.name AS mpa_name, " +
                "COUNT(l.user_id) AS like_count " +
                "FROM films f " +
                "JOIN film_directors fd ON f.film_id = fd.film_id " +
                "LEFT JOIN film_likes l ON f.film_id = l.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                "WHERE fd.director_id = ? " +
                "GROUP BY f.film_id, m.name " +
                "ORDER BY " + orderByClause;

        return jdbcTemplate.query(sql, this::mapRowToFilm, directorId);
    }

}