package com.example.learningsquad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class LearningsquadApplication {

    public static void main(String[] args) {
        SpringApplication.run(LearningsquadApplication.class, args);
    }

}
