package com.webapp.core.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * Database Initialization
 */
@Configuration
public class DatabaseInitializer {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    @Bean
    public CommandLineRunner initializeDatabase(DataSource dataSource) {
        return args -> {
            try {
                JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

                // Check if table exists
                try {
                    jdbcTemplate.queryForObject("SELECT COUNT(*) FROM USERS", Integer.class);
                    logger.info("Users table already exists");
                } catch (Exception e) {
                    // Table doesn't exist, create it
                    logger.info("Creating users table...");
                    jdbcTemplate.execute("CREATE TABLE USERS (" +
                           "ID INT PRIMARY KEY AUTO_INCREMENT, " +
                           "USERNAME VARCHAR(50) NOT NULL, " +
                           "PASSWORD VARCHAR(100) NOT NULL, " +
                           "EMAIL VARCHAR(100), " +
                           "ROLE VARCHAR(20) DEFAULT 'user')");

                    // Insert test data
                    jdbcTemplate.execute("INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ROLE) VALUES " +
                           "('admin', 'admin123', 'admin@example.com', 'admin')");
                    jdbcTemplate.execute("INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ROLE) VALUES " +
                           "('user1', 'password1', 'user1@example.com', 'user')");
                    jdbcTemplate.execute("INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ROLE) VALUES " +
                           "('user2', 'password2', 'user2@example.com', 'user')");
                    jdbcTemplate.execute("INSERT INTO USERS (USERNAME, PASSWORD, EMAIL, ROLE) VALUES " +
                           "('test', 'test123', 'test@example.com', 'user')");

                    logger.info("Database initialized with 4 test users");
                }

            } catch (Exception e) {
                logger.error("Database initialization error", e);
            }
        };
    }
}
