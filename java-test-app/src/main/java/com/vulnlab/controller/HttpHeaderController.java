package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/header")
public class HttpHeaderController {

    private static final Logger logger = LoggerFactory.getLogger(HttpHeaderController.class);

    @GetMapping("/set")
    public Map<String, Object> setHeader(@RequestParam("name") String name,
                                         HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Header set called with name: {}", name);

        response.setHeader("X-User-Name", name);

        result.put("message", "Header set");
        result.put("name", name);

        logger.warn("Header set: {}", name);
        return result;
    }

    @GetMapping("/cookie")
    public Map<String, Object> cookie(@RequestParam("name") String name,
                                      HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Cookie called with name: {}", name);

        response.setHeader("X-Username", name);

        result.put("message", "Cookie header set");
        result.put("name", name);

        logger.warn("Cookie header set: {}", name);
        return result;
    }

    @GetMapping("/body")
    public Map<String, Object> body(@RequestParam("content") String content) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Body called with content: {}", content);

        result.put("message", "Content received");
        result.put("content", content);

        logger.warn("Body content: {}", content);
        return result;
    }

    @GetMapping("/location")
    public Map<String, Object> location(@RequestParam("path") String path,
                                        HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Location called with path: {}", path);

        response.setHeader("Location", path);

        result.put("message", "Location header set");
        result.put("path", path);

        logger.warn("Location header set: {}", path);
        return result;
    }

    @GetMapping("/reflect")
    public Map<String, Object> reflect(@RequestParam("input") String input,
                                       HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Reflect input: {}", input);

        response.setHeader("X-Input", input);

        result.put("message", "Input reflected");
        result.put("input", input);

        logger.warn("Input reflected: {}", input);
        return result;
    }

    @GetMapping("/safe")
    public Map<String, Object> safe(@RequestParam("name") String name,
                                    HttpServletResponse response) {
        Map<String, Object> result = new HashMap<>();

        logger.info("Safe called with name: {}", name);

        String safeName = name.replaceAll("[\r\n]", "");
        response.setHeader("X-User-Name", safeName);

        result.put("message", "Header set safely");
        result.put("original", name);
        result.put("sanitized", safeName);

        logger.info("Safe header set: {}", safeName);
        return result;
    }
}
