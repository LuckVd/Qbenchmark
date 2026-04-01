package com.vulnlab.controller;

import org.apache.shiro.crypto.AesCipherService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/v1/session/data")
public class SessionDataController {

    private static final Logger logger = LoggerFactory.getLogger(SessionDataController.class);
    private static final byte[] DEFAULT_KEY = java.util.Base64.getDecoder().decode("kPH+bIxk5D2deZiIxcaaaA==");
    private static final String DELETE_ME = "deleteMe";
    private static final String REMEMBER_ME_COOKIE = "rememberMe";

    private final AesCipherService aesCipherService = new AesCipherService();

    @GetMapping("/decode")
    public String decode(HttpServletRequest request, HttpServletResponse response) {
        Cookie cookie = getCookie(request, REMEMBER_ME_COOKIE);
        if (cookie == null) {
            return "No session cookie. Right?";
        }

        String rememberMe = cookie.getValue();
        logger.info("Session cookie found, length: {}", rememberMe.length());

        try {
            byte[] b64DecodeRememberMe = java.util.Base64.getDecoder().decode(rememberMe);
            byte[] aesDecrypt = aesCipherService.decrypt(b64DecodeRememberMe, DEFAULT_KEY).getBytes();

            java.io.ByteArrayInputStream bytes = new java.io.ByteArrayInputStream(aesDecrypt);
            java.io.ObjectInputStream in = new java.io.ObjectInputStream(bytes);
            Object obj = in.readObject();
            in.close();

            logger.info("Session decode completed. Object: {}", obj.getClass().getName());
            return "Session decode completed. Object: " + obj.getClass().getName();

        } catch (Exception e) {
            logger.error("Session decode error", e);
            addCookie(response, REMEMBER_ME_COOKIE, DELETE_ME);
            return "Session decode error. Error: " + e.getMessage();
        }
    }

    private Cookie getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (name.equals(cookie.getName())) {
                    return cookie;
                }
            }
        }
        return null;
    }

    private boolean addCookie(HttpServletResponse response, String name, String value) {
        try {
            Cookie cookie = new Cookie(name, value);
            cookie.setPath("/");
            cookie.setHttpOnly(true);
            response.addCookie(cookie);
            return true;
        } catch (Exception e) {
            logger.error("Failed to add cookie", e);
            return false;
        }
    }

    @GetMapping("/info")
    public String info() {
        return "Session Data Decode\n" +
               "Cipher: AES\n" +
               "Default Key: kPH+bIxk5D2deZiIxcaaaA==\n" +
               "Endpoint: GET /api/v1/session/data/decode\n";
    }
}
