package com.vulnlab.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Base64;

@RestController
@RequestMapping("/api/v1/data/decode")
public class DataDecodeController {

    private static final Logger logger = LoggerFactory.getLogger(DataDecodeController.class);

    @PostMapping("/json")
    public String decodeJson(@RequestBody String payload) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enableDefaultTyping();

        try {
            Object obj = mapper.readValue(payload, Object.class);
            logger.info("JSON decode result: {}", obj);
            mapper.writeValueAsString(obj);
            return "JSON decode completed. Object: " + obj.getClass().getName();
        } catch (IOException e) {
            logger.error("JSON decode error", e);
            return "JSON decode error: " + e.getMessage();
        }
    }

    @GetMapping("/session")
    public String decodeSession(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "No session cookie found.";
        }

        for (Cookie cookie : cookies) {
            if ("rememberMe".equals(cookie.getName())) {
                String rememberMe = cookie.getValue();
                logger.info("Session cookie found, length: {}", rememberMe.length());

                try {
                    byte[] decoded = Base64.getDecoder().decode(rememberMe);
                    ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);
                    ObjectInputStream in = new ObjectInputStream(bytes);
                    Object obj = in.readObject();
                    in.close();

                    logger.info("Decoded object: {}", obj.getClass().getName());
                    return "Decode completed. Object: " + obj.getClass().getName();
                } catch (Exception e) {
                    logger.error("Decode error", e);
                    return "Decode error: " + e.getMessage();
                }
            }
        }

        return "No session cookie found.";
    }

    @GetMapping("/session/vuln")
    public String decodeSessionVuln(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return "No session cookie. Right?";
        }

        for (Cookie cookie : cookies) {
            if ("rememberMe".equals(cookie.getName())) {
                String rememberMe = cookie.getValue();
                logger.info("Session cookie: {}", rememberMe.substring(0, Math.min(20, rememberMe.length())));

                try {
                    byte[] decoded = Base64.getDecoder().decode(rememberMe);
                    ByteArrayInputStream bytes = new ByteArrayInputStream(decoded);
                    ObjectInputStream in = new ObjectInputStream(bytes);
                    in.readObject();
                    in.close();

                    return "Are u ok?";
                } catch (Exception e) {
                    logger.error("Decode error", e);
                    return "Decode error: " + e.getMessage();
                }
            }
        }

        return "No session cookie. Right?";
    }
}
