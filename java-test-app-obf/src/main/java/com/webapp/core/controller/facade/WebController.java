package com.webapp.core.controller.facade;

import com.webapp.core.controller.executor.WebOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * Web Controller - Web feature APIs
 */
@RestController
@RequestMapping("/api/v1")
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private WebOps webOps;

    @GetMapping("/redirect")
    public String redirect(@RequestParam("url") String url) {
        return webOps.handleRedirect(url);
    }

    @PostMapping("/file/upload")
    public String upload(@RequestParam("file") String file) {
        return webOps.handleUpload(file);
    }

    @GetMapping("/file/list")
    public String listFiles() {
        return webOps.listFiles();
    }

    @PostMapping("/auth/verify")
    public String verify(@RequestBody String token) {
        return webOps.verifyToken(token);
    }

    @GetMapping("/cors")
    public String cors() {
        return webOps.handleCors();
    }

    @PostMapping("/form/submit")
    public String submit(@RequestParam("data") String data) {
        return webOps.handleForm(data);
    }

    @GetMapping("/header/set")
    public String setHeader(HttpServletRequest request) {
        return webOps.setHeader(request);
    }
}
