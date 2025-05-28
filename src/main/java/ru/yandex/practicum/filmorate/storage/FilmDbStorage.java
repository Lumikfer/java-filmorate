package ru.yandex.practicum.filmorate.storage;

import jakarta.validation.ValidationException;
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

import java.sql.*;
import java.sql.Date;
import java.util.*;

@Repository
@Primary
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbcTemplate;
    private final GenreStorage genreStorage;
    private final UserStorage userStorage;
    private final DirectorStorage directorStorage;

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
        updateGenres(film);
        updateDirectors(film);
        return film;
    }

    @Override
    public Film updateFilm(Film film) {
        getFilmOrThrow(film.getId());
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
        updateGenres(film);
        updateDirectors(film);
        return film;
    }

    private Film getFilmOrThrow(int id) {
        Film film = getFilmById(id);
        if (film == null) {
            throw new NotFoundException("Film not found with id: " + id);
        }
        return film;
    }

    private void updateGenres(Film film) {
        jdbcTemplate.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        List<Genre> genres = new ArrayList<>(film.getGenres());
        genres.sort(Comparator.comparingInt(Genre::getId));
        for (Genre genre : genres) {
            jdbcTemplate.update(
                    "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)",
                    film.getId(),
                    genre.getId()
            );
        }
    }

    private void updateDirectors(Film film) {
        jdbcTemplate.update("DELETE FROM film_directors WHERE film_id = ?", film.getId());
        List<Director> directors = new ArrayList<>(film.getDirectors());
        directors.sort(Comparator.comparingInt(Director::getId));
        for (Director director : directors) {
            jdbcTemplate.update(
                    "INSERT INTO film_directors (film_id, director_id) VALUES (?, ?)",
                    film.getId(),
                    director.getId()
            );
        }
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
        return jdbcTemplate.queryForObject(sql, this::mapRowToFilm, id);
    }

    private Film mapRowToFilm(ResultSet rs, int rowNum) throws SQLException {
        Film film = new Film();
        film.setId(rs.getInt("film_id"));
        film.setName(rs.getString("name"));
        film.setDescription(rs.getString("description"));
        film.setReleaseDate(rs.getDate("release_date").toLocalDate());
        film.setDuration(rs.getInt("duration"));
        film.setMpa(new Mpa(rs.getInt("mpa_id"), rs.getString("mpa_name")));
        Set<Integer> likes = getLikes(film.getId());
        film.setLike(likes);
        List<Genre> genres = new ArrayList<>(genreStorage.getFilmGenres(film.getId()));
        genres.sort(Comparator.comparingInt(Genre::getId));
        film.setGenres(genres);
        List<Director> directors = new ArrayList<>(directorStorage.getDirectorsByFilmId(film.getId()));
        directors.sort(Comparator.comparingInt(Director::getId));
        film.setDirectors(directors);

        return film;
    }

    private Set<Integer> getLikes(int filmId) {
        String sql = "SELECT user_id FROM film_likes WHERE film_id = ?";
        return new HashSet<>(jdbcTemplate.queryForList(sql, Integer.class, filmId));
    }

    @Override
    public void deleteFilmById(int id) {
        String checkSql = "SELECT COUNT(*) FROM films WHERE film_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, id);

        if (count == 0) {
            throw new NotFoundException("Film with id " + id + " not found");
        }

        String sql = "DELETE FROM films WHERE film_id = ?";
        jdbcTemplate.update(sql, id);
    }

    @Override
    public void addLike(int filmId, int userId) {

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден.");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }

        String checkSql = "SELECT COUNT(*) FROM film_likes WHERE film_id = ? AND user_id = ?";
        Integer count = jdbcTemplate.queryForObject(checkSql, Integer.class, filmId, userId);
        if (count != null && count > 0) {
            throw new ValidationException("Лайк уже поставлен.");
        }


        String sql = "INSERT INTO film_likes (film_id, user_id) VALUES (?, ?)";
        jdbcTemplate.update(sql, filmId, userId);
        film.getLike().add(userId);
    }


    @Override
    public void removeLike(int filmId, int userId) {

        Film film = getFilmById(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с id=" + filmId + " не найден.");
        }
        if (userStorage.getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }

        String sql = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";
        int rowsDeleted = jdbcTemplate.update(sql, filmId, userId);
        if (rowsDeleted == 0) {
            throw new NotFoundException("Лайк не найден.");
        }
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
        if (year != 1 && genreId != 1) {
            newsql = "WHERE fg.genre_id = ? AND EXTRACT(YEAR FROM f.release_date) = ? ";
            params.add(genreId);
            params.add(year);
        }
          
        String sql = "SELECT f.film_id, f.name, f.description, f.release_date, f.duration, f.mpa_id, m.name" +
                " AS mpa_name " +
                "FROM films f " +
                "JOIN film_genres fg ON f.film_id = fg.film_id " +
                "JOIN film_likes fl ON f.film_id = fl.film_id " +
                "JOIN mpa m ON f.mpa_id = m.mpa_id " +
                newsql +
                "GROUP BY f.film_id " +
                "ORDER BY COUNT(fl.user_id) DESC " +
                "LIMIT ?";

        params.add(count);

        return jdbcTemplate.query(sql, this::mapRowToFilm, params.toArray());
    }


    @Override
    public List<Film> getFilmsByDirectorId(int directorId, String sortBy) {
        String orderByClause;
        switch (sortBy.toLowerCase()) {
            case "year":
                orderByClause = "f.release_date";
                break;
            case "likes":
                orderByClause = "like_count DESC";
                break;
            default:
                throw new IllegalArgumentException("Неподдерживаемый параметр сортировки: " + sortBy);
        }

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