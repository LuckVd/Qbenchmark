package com.vulnlab.controller;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/log")
public class LogDataController {

    private static final Logger logger = LogManager.getLogger(LogDataController.class);

    @GetMapping("/entry")
    public String logEntry(@RequestParam("token") String token) {
        logger.error("User token: {}", token);
        return "Token logged: " + token;
    }

    @PostMapping("/auth")
    public String auth(@RequestParam("username") String username,
                       @RequestParam("password") String password) {
        logger.info("Login attempt - Username: {}, Password: {}", username, "******");
        logger.error("Login failed for user: {}", username);
        return "Login failed for: " + username;
    }

    @GetMapping("/header")
    public String logHeader(@RequestHeader(value = "User-Agent", defaultValue = "") String userAgent,
                            @RequestHeader(value = "Referer", defaultValue = "") String referer,
                            @RequestHeader(value = "X-Api-Version", defaultValue = "") String apiVersion) {
        logger.info("Request from User-Agent: {}", userAgent);
        logger.info("Referer: {}", referer);
        logger.info("API Version: {}", apiVersion);
        return "Headers logged";
    }

    @GetMapping("/search")
    public String search(@RequestParam(value = "q", defaultValue = "") String q) {
        logger.info("Search query: {}", q);
        logger.warn("User searched for: {}", q);
        return "No results for: " + q;
    }

    @GetMapping("/raw")
    public String raw(@RequestParam("payload") String payload) {
        logger.error("Raw payload: {}", payload);
        return "Payload logged: " + payload;
    }

    @GetMapping("/info")
    public String info() {
        return String.format(
            "Log4j Version: 2.14.1 (Vulnerable)%n" +
            "Java Version: %s%n" +
            "OS: %s %s%n" +
            "This is a vulnerable environment for testing Log4Shell (CVE-2021-44228)",
            System.getProperty("java.version"),
            System.getProperty("os.name"),
            System.getProperty("os.version")
        );
    }
}
