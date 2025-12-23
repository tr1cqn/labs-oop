package com.example.lab6.security;

import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import repository.UserRepository;

/**
 * Создает bootstrap ADMIN пользователя, если его нет.
 */
@Component
public class AdminInitializer implements CommandLineRunner {
    private static final Logger logger = LogManager.getLogger(AdminInitializer.class);
    private final UserRepository userRepository;

    public AdminInitializer(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public void run(String... args) {
        final String login = "admin";
        final String password = "admin";
        final String email = "admin@example.com";

        userRepository.findByLogin(login).ifPresentOrElse(
                u -> logger.info("Admin user already exists: {}", login),
                () -> {
                    User admin = new User(login, password, email);
                    admin.setRole("ADMIN");
                    userRepository.save(admin);
                    logger.warn("Создан bootstrap ADMIN пользователь login=admin password=admin (смените пароль)");
                }
        );
    }
}


