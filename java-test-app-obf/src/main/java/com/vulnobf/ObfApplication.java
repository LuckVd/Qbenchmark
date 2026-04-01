package com.vulnobf;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Obfuscated Vulnerability Lab Application
 *
 * Port: 8081
 * Purpose: Test static analysis tools against obfuscated vulnerability code
 */
@SpringBootApplication
public class ObfApplication {

    public static void main(String[] args) {
        SpringApplication.run(ObfApplication.class, args);
    }
}
