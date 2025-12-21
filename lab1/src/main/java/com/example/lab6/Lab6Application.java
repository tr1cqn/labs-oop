package com.example.lab6;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.ComponentScan;

/**
 * Главный класс Spring Boot приложения
 */
@SpringBootApplication
@EntityScan(basePackages = "entity")
@ComponentScan(basePackages = {"com.example.lab6", "repository"})
public class Lab6Application {

    public static void main(String[] args) {
        SpringApplication.run(Lab6Application.class, args);
    }
}

