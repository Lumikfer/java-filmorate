package ru.yandex.practicum.filmorate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.FileCopyUtils;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Statement;

@SpringBootApplication
public class FilmorateApplication  implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    public static void main(String[] args) {
        SpringApplication.run(FilmorateApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        ClassPathResource resource = new ClassPathResource("schema.sql");
        ClassPathResource resources = new ClassPathResource("data.sql");
        String sql = new String(FileCopyUtils.copyToByteArray(resource.getInputStream()));
        String sql1 = new String(FileCopyUtils.copyToByteArray(resources.getInputStream()));

        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            stmt.execute(sql1);
        }
    }

}
