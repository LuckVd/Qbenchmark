package com.webapp.core;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Web Application Platform - Main Entry Point
 *
 * A unified web platform providing data processing, content management,
 * and system integration services for enterprise applications.
 *
 * Port: 8081
 */
@SpringBootApplication
public class WebApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebApplication.class, args);
        System.out.println("\n========================================");
        System.out.println("  Web Application Platform Started!");
        System.out.println("  http://localhost:8081");
        System.out.println("========================================\n");
    }
}
