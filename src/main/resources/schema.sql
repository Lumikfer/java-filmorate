
DROP TABLE IF EXISTS film_likes CASCADE;
DROP TABLE IF EXISTS film_genres CASCADE;
DROP TABLE IF EXISTS friends CASCADE;
DROP TABLE IF EXISTS films CASCADE;
DROP TABLE IF EXISTS users CASCADE;
DROP TABLE IF EXISTS genres CASCADE;
DROP TABLE IF EXISTS mpa CASCADE;

CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    email VARCHAR(50) NOT NULL,
    login VARCHAR(50) NOT NULL,
    name VARCHAR(50) NOT NULL,
    birthday DATE,
    CONSTRAINT users_unique_email UNIQUE (email),
    CONSTRAINT users_unique_login UNIQUE (login)
);

CREATE TABLE IF NOT EXISTS friends (
    user_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    friend_id BIGINT REFERENCES users(user_id) ON DELETE CASCADE,
    CONSTRAINT friends_pk PRIMARY KEY (user_id, friend_id)
);

CREATE TABLE IF NOT EXISTS mpa (
    mpa_id SERIAL PRIMARY KEY,  -- Изменено с id на mpa_id
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS films (
    film_id SERIAL PRIMARY KEY,  -- Изменено с id на film_id
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    release_date DATE,
    duration INT CHECK (duration > 0),
    mpa_id INT,
    FOREIGN KEY (mpa_id) REFERENCES mpa(mpa_id) ON DELETE SET NULL  -- Исправлено
);

CREATE TABLE IF NOT EXISTS genres (
    genre_id SERIAL PRIMARY KEY,  -- Изменено с id на genre_id
    name VARCHAR(100) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS film_genres (
    film_id INT NOT NULL REFERENCES films(film_id) ON DELETE CASCADE,  -- Исправлено
    genre_id INT NOT NULL REFERENCES genres(genre_id) ON DELETE CASCADE,  -- Исправлено
    PRIMARY KEY (film_id, genre_id)
);

CREATE TABLE film_likes (
    film_id INT NOT NULL,
    user_id INT NOT NULL,
    PRIMARY KEY (film_id, user_id),
    FOREIGN KEY (film_id) REFERENCES films(film_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);