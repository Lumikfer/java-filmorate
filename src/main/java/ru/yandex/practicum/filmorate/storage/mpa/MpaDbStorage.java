package ru.yandex.practicum.filmorate.storage.mpa;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

@Repository
@RequiredArgsConstructor
@Slf4j
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;


    @Override
    public List<Mpa> getAllMpa() {
        String sql = "SELECT * FROM mpa ORDER BY mpa_id ASC";
        return jdbcTemplate.query(sql, this::mapRowToMpa);
    }

    @Override
    public Mpa getMpaById(Integer id) {
        log.debug("Fetching MPA with id: {}", id);
        try {
            String sql = "SELECT mpa_id, name FROM mpa WHERE mpa_id = ?";
            Mpa mpa = jdbcTemplate.queryForObject(sql, this::mapRowToMpa, id);
            log.debug("Found MPA: {}", mpa);
            return mpa;
        } catch (EmptyResultDataAccessException e) {
            log.error("MPA not found. ID: {}", id);
            throw new NotFoundException("MPA not found with id: " + id);
        }
    }

    private Mpa mapRowToMpa(ResultSet rs, int rowNum) throws SQLException {
        return new Mpa(rs.getInt("mpa_id"), rs.getString("name"));
    }
}