package com.example.lab6.security;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private static final Logger logger = LogManager.getLogger(SecurityConfig.class);

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.info("Configuring HTTP security (Basic Auth)");
        http.csrf().disable();
        http.authorizeRequests()
                .antMatchers("/h2-console/**").permitAll()
                .antMatchers("/api/v1/test").permitAll()
                .antMatchers("/api/v1/users").permitAll() // регистрация
                .anyRequest().authenticated()
                .and()
                .httpBasic()
                .and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

        // H2 console frame support
        http.headers().frameOptions().sameOrigin();
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // В учебных целях храним пароли в открытом виде (совместимо с уже существующими данными)
        return NoOpPasswordEncoder.getInstance();
    }
}


