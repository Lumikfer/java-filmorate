INSERT INTO mpa (name) VALUES
                         ('G'),
                         ('PG'),
                         ('PG-13'),
                         ('R'),
                         ('NC-17');


                         INSERT INTO genres (name) VALUES
                         ('Комедия'),
                         ('Драма'),
                         ('Мультфильм'),
                         ('Триллер'),
                         ('Документальный'),
                         ('Боевик');


                         INSERT INTO users (name, email, login, birthday) VALUES
                         ('John Doe', 'john@example.com', 'john_doe', '1990-01-01'),
                         ('Jane Doe', 'jane@example.com', 'jane_doe', '1992-02-02');


                         INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES
                         ('Film 1', 'Description for Film 1', '2023-01-01', 120, 1),
                         ('Film 2', 'Description for Film 2', '2023-02-01', 90, 2),
                         ('Film 3', 'Description for Film 3', '2023-03-01', 150, 3);


                         INSERT INTO film_genres (film_id, genre_id) VALUES
                         (1, 1),
                         (1, 3),
                         (2, 2),
                         (3, 5);


                         INSERT INTO user_friends (user_id, friend_id) VALUES (1, 2);