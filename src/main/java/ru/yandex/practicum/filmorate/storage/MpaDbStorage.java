package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;

@Repository
@RequiredArgsConstructor
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    @Override
    public Collection<Mpa> getAllMpa() {

        String sql = "SELECT * FROM ratings";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Mpa getMpaById(int id) {
        try {
            String sql = "SELECT * FROM ratings WHERE rating_id = ?";
            return jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotFoundException("MPA not found with id: " + id);
        }
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {

        return new Mpa(rs.getInt("rating_id"), rs.getString("name"));
    }
}