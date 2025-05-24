package ru.yandex.practicum.filmorate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;

@SpringBootApplication
public class FilmorateApplication   {


    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }

}
