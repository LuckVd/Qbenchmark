package com.vulnlab.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/nav")
public class NavController {

    private static final Logger logger = LoggerFactory.getLogger(NavController.class);

    @GetMapping("/goto")
    public void gotoUrl(@RequestParam("url") String url, HttpServletResponse response) {
        logger.info("Redirect called with url: {}", url);

        try {
            response.sendRedirect(url);
        } catch (Exception e) {
            logger.error("Redirect error", e);
        }
    }

    @GetMapping("/location")
    public String location(@RequestParam("url") String url, HttpServletResponse response) {
        logger.info("Location called with url: {}", url);

        try {
            response.setHeader("Location", url);
            return "Redirecting to: " + url;
        } catch (Exception e) {
            logger.error("Location redirect error", e);
            return "Redirect error: " + e.getMessage();
        }
    }

    @GetMapping("/forward")
    public String forward(@RequestParam("path") String path, HttpServletRequest request) {
        logger.info("Forward called with path: {}", path);

        try {
            request.getRequestDispatcher(path).forward(request, (HttpServletResponse) request.getAttribute("javax.servlet.jsp.PageContext"));
            return "Forwarding to: " + path;
        } catch (Exception e) {
            logger.error("Forward error", e);
            return "Forward error: " + e.getMessage();
        }
    }

    @GetMapping("/relative")
    public void relative(@RequestParam("path") String path, HttpServletResponse response) {
        logger.info("Relative redirect called with path: {}", path);

        try {
            response.sendRedirect(path);
        } catch (Exception e) {
            logger.error("Relative redirect error", e);
        }
    }
}
