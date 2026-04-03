package com.webapp.core.controller.executor;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.crypto.MacProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;

/**
 * Web Operations - Web feature handlers
 */
@Component
public class WebOps {

    private static final Logger logger = LoggerFactory.getLogger(WebOps.class);
    private static final Key key = MacProvider.generateKey();

    public String handleRedirect(String url) {
        return "Redirecting to: " + url;
    }

    public String handleUpload(String file) {
        return "File uploaded: " + file;
    }

    public String listFiles() {
        return "Files: test.txt, data.json";
    }

    public String verifyToken(String token) {
        try {
            Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            return "Token valid";
        } catch (Exception e) {
            return "Token invalid: " + e.getMessage();
        }
    }

    public String handleCors() {
        return "CORS: Access-Control-Allow-Origin: *";
    }

    public String handleForm(String data) {
        return "Form submitted: " + data;
    }

    public String setHeader(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        return "Header set. User-Agent: " + userAgent;
    }
}
