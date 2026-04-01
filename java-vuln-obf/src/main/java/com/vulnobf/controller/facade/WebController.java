package com.vulnobf.controller.facade;

import com.vulnobf.controller.executor.WebOps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

/**
 * Web Controller - Web vulnerability endpoints
 */
@RestController
@RequestMapping("/api/v1")
public class WebController {

    private static final Logger logger = LoggerFactory.getLogger(WebController.class);

    @Autowired
    private WebOps webOps;

    /**
     * URL redirect
     * Path: /api/v1/redirect
     * Original: /urlRedirect/*
     */
    @GetMapping("/redirect")
    public String redirect(@RequestParam("url") String url) {
        logger.info("Redirect to: {}", url);
        return "redirect:" + url;
    }

    /**
     * File upload
     * Path: /api/v1/file/upload
     * Original: /file/upload
     */
    @PostMapping("/file/upload")
    public String upload(@RequestParam("file") MultipartFile file,
                         @RequestParam(value = "path", defaultValue = "/tmp/uploads") String path) {
        logger.info("Upload file: {}", file.getOriginalFilename());
        return webOps.uploadFile(file, path);
    }

    /**
     * JWT parse
     * Path: /api/v1/auth/verify
     * Original: /jwt/parse
     */
    @PostMapping("/auth/verify")
    public String verifyJwt(@RequestBody String token) {
        logger.info("Verify JWT");
        return webOps.parseJwt(token);
    }

    /**
     * CORS test
     * Path: /api/v1/cors
     * Original: /cors/test
     */
    @GetMapping("/cors")
    public String cors(@RequestHeader(value = "Origin", defaultValue = "*") String origin) {
        return webOps.checkCors(origin);
    }

    /**
     * CSRF form
     * Path: /api/v1/form/submit
     * Original: /csrf/vuln
     */
    @PostMapping("/form/submit")
    public String submit(@RequestParam("data") String data,
                        @RequestParam(value = "token", required = false) String token) {
        logger.info("Form submit");
        return webOps.csrfAction(data, token);
    }

    /**
     * CRLF injection
     * Path: /api/v1/header/set
     * Original: /crlf/injection
     */
    @GetMapping("/header/set")
    public String setHeader(@RequestParam("value") String value) {
        logger.info("Set header");
        return webOps.setHeader(value);
    }
}
