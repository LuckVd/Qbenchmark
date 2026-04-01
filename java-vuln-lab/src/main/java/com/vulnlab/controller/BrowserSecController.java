package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/browser")
public class BrowserSecController {

    private static final Logger logger = LoggerFactory.getLogger(BrowserSecController.class);

    @GetMapping("/cors")
    public Map<String, Object> cors(HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
        response.setHeader("Access-Control-Allow-Headers", "*");
        response.setHeader("Access-Control-Allow-Credentials", "true");

        result.put("message", "Data exposed");
        result.put("data", "Sensitive data");

        logger.info("CORS request");
        return result;
    }

    @GetMapping("/origin")
    public Map<String, Object> origin(@RequestHeader(value = "Origin", defaultValue = "*") String origin,
                                      HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        response.setHeader("Access-Control-Allow-Origin", origin);
        response.setHeader("Access-Control-Allow-Credentials", "true");

        result.put("message", "Origin reflected");
        result.put("origin", origin);

        logger.info("Origin: {}", origin);
        return result;
    }

    @GetMapping("/form")
    public Map<String, Object> form(@RequestParam("action") String action,
                                    @RequestParam(value = "to", defaultValue = "unknown") String to,
                                    @RequestParam(value = "amount", defaultValue = "0") String amount) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Form action: {} to {}, amount: {}", action, to, amount);

        result.put("action", action);
        result.put("to", to);
        result.put("amount", amount);
        result.put("status", "success");

        return result;
    }

    @GetMapping("/cookie/set")
    public Map<String, Object> setCookie(@RequestParam("name") String name,
                                         @RequestParam("value") String value,
                                         HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        response.addCookie(cookie);

        result.put("message", "Cookie set");
        result.put("name", name);
        result.put("value", value);

        logger.info("Cookie set: {}={}", name, value);
        return result;
    }

    @GetMapping("/cookie/profile")
    public Map<String, Object> profile(@CookieValue(value = "user", defaultValue = "guest") String user) {
        Map<String, Object> result = new HashMap<>();

        result.put("username", user);
        result.put("role", user.equals("admin") ? "administrator" : "user");
        result.put("data", "Data for " + user);

        logger.info("Profile: {}", user);
        return result;
    }
}
