package com.vulnlab.controller;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/api/v1/web")
public class WebRenderController {

    private static final Logger logger = LoggerFactory.getLogger(WebRenderController.class);

    @GetMapping("/render")
    @ResponseBody
    public String render(@RequestParam(value = "content", defaultValue = "test") String content) {
        logger.info("Render input: {}", content);
        return content;
    }

    @PostMapping("/render")
    @ResponseBody
    public String renderPost(@RequestParam("data") String data) {
        logger.info("Render POST input: {}", data);
        return data;
    }

    @GetMapping("/store")
    @ResponseBody
    public String store(@RequestParam("data") String data, HttpServletResponse response) {
        logger.info("Storing data: {}", data);

        Cookie cookie = new Cookie("web_data", data);
        cookie.setMaxAge(3600);
        cookie.setPath("/");
        response.addCookie(cookie);

        return "Data stored. Access /api/v1/web/show to retrieve.";
    }

    @GetMapping("/show")
    @ResponseBody
    public String show(@CookieValue(value = "web_data", defaultValue = "No data found") String data) {
        logger.info("Retrieved data: {}", data);
        return data;
    }

    @GetMapping("/search")
    @ResponseBody
    public String search(@RequestParam(value = "q", defaultValue = "") String q) {
        logger.info("Search query: {}", q);

        StringBuilder html = new StringBuilder();
        html.append("<html><head><title>Search Results</title></head><body>");
        html.append("<h1>Results for: ").append(q).append("</h1>");
        html.append("<p>No results found.</p>");
        html.append("</body></html>");

        return html.toString();
    }

    @GetMapping("/render/sec")
    @ResponseBody
    public String renderSecure(@RequestParam(value = "content", defaultValue = "test") String content) {
        logger.info("Render input (secure): {}", content);
        return htmlEncode(content);
    }

    private String htmlEncode(String input) {
        if (input == null) {
            return "";
        }
        input = StringUtils.replace(input, "&", "&amp;");
        input = StringUtils.replace(input, "<", "&lt;");
        input = StringUtils.replace(input, ">", "&gt;");
        input = StringUtils.replace(input, "\"", "&quot;");
        input = StringUtils.replace(input, "'", "&#x27;");
        input = StringUtils.replace(input, "/", "&#x2F;");
        return input;
    }
}
